import * as path from 'node:path';
import * as fs from 'node:fs';

import { PACKAGE, INDENT, NUMBER_TYPE, ClassInfo, classes, TokenFinder, srcPath } from './common';

// Mislabeled Fields
const MISLABELED_FIELDS = [
    'Person.inbox_url',
    'Community.followers_url',
    'Community.inbox_url'
];

// Class Information
class APIClassField {
    readonly type: string;
    readonly name: string;
    readonly nullable: boolean;

    constructor(type: string, name: string, nullable: boolean) {
        this.type = APIClassField.fixType(type);
        this.name = name;
        this.nullable = nullable;
    }

    toString() {
        const annotation = this.nullable ? '@Nullable' : '@NotNull';
        const declaration = `public ${this.type} ${this.name};`;
        return INDENT + annotation + '\n' + INDENT + declaration + '\n';
    }

    // Convert TS Type To Java Type
    private static fixType(type: string) {
        type = type.replace(/number/g, NUMBER_TYPE);
        type = type.replace(/string/g, 'String');
        type = type.replace(/boolean/g, 'Boolean');
        type = type.replace(/Array/g, 'List');
        if (type.endsWith('Id')) {
            type = NUMBER_TYPE;
        }
        if (type.endsWith('[]')) {
            type = `List<${type.replace(/\[\]/g, '')}>`;
        }
        if (type.startsWith('List<') && type.endsWith('Id>')) {
            type = `List<${NUMBER_TYPE}>`;
        }
        return type;
    }
}
class APIClassInfo implements ClassInfo {
    readonly name: string;
    readonly fields: APIClassField[];
    javadoc: string | null;
    isMethod: boolean;
    responseClassType: string | null;
    path: string | null;
    httpType: string | null;
    usesToken: boolean;
    requiresToken: boolean;

    constructor(name: string) {
        this.name = name;
        this.fields = [];
        this.javadoc = null;
        // Method
        this.isMethod = false;
        this.responseClassType = null;
        this.path = null;
        this.httpType = null;
        // AuthenticatedMethod
        this.usesToken = false;
        this.requiresToken = false;
    }

    getParent() {
        if (this.isMethod) {
            let parent = null;
            if (this.usesToken) {
                parent = 'AuthenticatedMethod';
            } else {
                parent = 'Method';
            }
            // Generic Information
            parent += `<${this.responseClassType}>`;
            // Return
            return parent;
        } else {
            // No Parent Class
            return null;
        }
    }

    toString() {
        let data = '';

        // JavaDoc
        if (this.javadoc != null) {
            data += '/**\n';
            const lines = this.javadoc.split('\n');
            for (const line of lines) {
                data += ` * ${line}\n`;
            }
            data += ' */\n';
        }

        // Suppress Warning If Needed
        data += '@SuppressWarnings({"NotNullFieldNotInitialized", "unused", "RedundantSuppression"})\n';

        // Open Class
        data += `public class ${this.name} `;
        const parent = this.getParent();
        if (parent != null) {
            data += `extends ${parent} `;
        }
        data += 'implements Verifiable {\n';

        // Fields
        for (const field of this.fields) {
            data += field.toString();
        }

        // Methods
        data += this.generateMethods();

        // Close Class
        data += '}\n';

        // Imports
        data = this.generateImports(data) + data;

        // Package
        data = `package ${PACKAGE};\n\n` + data;

        // Return
        return data;
    }

    generateImports(existingData: string) {
        const imports = ['com.thebrokenrail.combustible.api.util.*'];
        if (existingData.includes('@Nullable')) {
            imports.push('org.jetbrains.annotations.Nullable');
        }
        if (existingData.includes('@NotNull')) {
            imports.push('org.jetbrains.annotations.NotNull');
        }
        if (existingData.includes('List<')) {
            imports.push('java.util.List');
        }
        // Return
        let data = '';
        for (const str of imports) {
            data += `import ${str};\n`;
        }
        data += '\n';
        return data;
    }

    generateMethods() {
        let data = '';

        // Method
        if (this.isMethod) {
            data += INDENT + '@Override\n';
            data += INDENT + `public Class<${this.responseClassType}> getResponseClass() {\n`;
            data += INDENT + INDENT + `return ${this.responseClassType}.class;\n`;
            data += INDENT + '}\n';
            data += INDENT + '@Override\n';
            data += INDENT + 'public String getPath() {\n';
            data += INDENT + INDENT + `return "${this.path}";\n`;
            data += INDENT + '}\n';
            data += INDENT + '@Override\n';
            data += INDENT + 'public Type getType() {\n';
            data += INDENT + INDENT + `return Type.${this.httpType};\n`;
            data += INDENT + '}\n';
            // Authentication
            if (this.usesToken) {
                data += INDENT + '@Override\n';
                data += INDENT + 'public boolean requiresToken() {\n';
                data += INDENT + INDENT + `return ${this.requiresToken};\n`;
                data += INDENT + '}\n';
            }
        }

        // Verification
        data += INDENT + '@SuppressWarnings("ConstantConditions")\n';
        data += INDENT + '@Override\n';
        data += INDENT + 'public void verify() {\n';
        for (const field of this.fields) {
            // Null Check
            if (!field.nullable) {
                data += INDENT + INDENT + `if (${field.name} == null) {\n`;
                data += INDENT + INDENT + INDENT + `throw new RuntimeException("${this.name}.${field.name} is null");\n`;
                data += INDENT + INDENT + '}\n';
            }

            // Verify Lists
            const listPrefix = 'List<';
            if (field.type.startsWith(listPrefix)) {
                // Check If List Type Is Verifiable
                const listType = field.type.substring(listPrefix.length, field.type.length - 1);
                const listTypeInfo = classes[listType];
                if (listTypeInfo && listTypeInfo instanceof APIClassInfo) {
                    // Field Is A List
                    let forIndent = INDENT + INDENT;
                    // Null Check If Needed
                    if (field.nullable) {
                        data += INDENT + INDENT + `if (${field.name} != null) {\n`;
                        forIndent += INDENT;
                    }

                    // Iterate Over List
                    data += forIndent + `for (${listType} obj : ${field.name}) {\n`;
                    data += forIndent + INDENT + 'obj.verify();\n';
                    data += forIndent + '}\n';

                    // End Null Check
                    if (field.nullable) {
                        data += INDENT + INDENT + '}\n';
                    }
                }
            }

            // Recursive Verification
            const fieldTypeInfo = classes[field.type];
            if (fieldTypeInfo && fieldTypeInfo instanceof APIClassInfo) {
                let verifyIndent = INDENT + INDENT;
                if (field.nullable) {
                    data += INDENT + INDENT + `if (${field.name} != null) {\n`;
                    verifyIndent += INDENT;
                }
                data += verifyIndent + `${field.name}.verify();\n`;
                if (field.nullable) {
                    data += INDENT + INDENT + '}\n';
                }
            }
        }
        data += INDENT + '}\n';

        // Return
        return data;
    }
}

// Load Function
export function load(definitions: string) {
    // Locate Classes
    let classInfo = null;
    for (let line of definitions.split('\n')) {
        line = line.trim();
        if (line === '}' && classInfo !== null) {
            // End Class
            classes[classInfo.name] = classInfo;
            classInfo = null;
        } else if (line.startsWith('export interface ') && classInfo === null) {
            // Start Class
            const name = line.split(' ')[2];
            if (name === undefined) {
                continue;
            }
            classInfo = new APIClassInfo(name);
        } else if (classInfo !== null && !line.startsWith('/**') && !line.startsWith('*') && !line.startsWith('*/')) {
            // Read Class Property
            let nullable = line.indexOf('?:') !== -1;
            line = line.replace(/;/g, '');
            line = line.replace(/\?/g, '');
            const parts = line.split(': ');
            const type = parts[1];
            const name = parts[0];
            if (type === undefined || name === undefined) {
                continue;
            }
            if (name === 'auth') {
                // Authentication Token Is Treated Separately
                classInfo.usesToken = true;
                if (!nullable) {
                    classInfo.requiresToken = true;
                }
            } else {
                // Some Fields Are Mislabeled As Non-Null
                if (MISLABELED_FIELDS.includes(classInfo.name + '.' + name)) {
                    nullable = true;
                }

                // Add Field
                classInfo.fields.push(new APIClassField(type, name, nullable));
            }
        }
    }

    // Read http.ts
    const httpTs = fs.readFileSync(path.join(srcPath, 'http.ts'), {encoding: 'utf8'});

    // Read Type Information From http.ts
    const finder = new TokenFinder(httpTs);
    while (true) {
        // Search
        const str = finder.next('this.#wrapper<');
        if (str === null) {
            break;
        }

        // Find Types
        let piece = finder.next('>');
        if (piece === null) {
            break;
        }
        piece = piece.replace(/ /g, '');
        piece = piece.replace(/\n/g, '');
        const pieces = piece.split(',');
        const requestClassType = pieces[0];
        let responseClassType = pieces[1];
        if (responseClassType === undefined || requestClassType === undefined) {
            break;
        }
        if (!classes[responseClassType]) {
            responseClassType = 'Object';
        }

        // Find HTTP Type
        finder.next('HttpType.');
        const httpType = finder.next(',')?.toUpperCase();
        if (httpType === undefined) {
            break;
        }

        // Find Path
        finder.next('"/');
        const path = finder.next('"');

        // Add Information To Class
        const data = classes[requestClassType];
        if (data instanceof APIClassInfo) {
            data.isMethod = true;
            data.responseClassType = responseClassType;
            data.path = path;
            data.httpType = httpType;

            // JavaDoc
            const openingTag = '/**';
            const closingTag = '*/';
            const openPosition = str.lastIndexOf(openingTag);
            const closePosition = str.lastIndexOf(closingTag);
            if (openPosition !== -1 && closePosition !== -1) {
                // Process JavaDoc
                const rawJavadoc = str.substring(openPosition + openingTag.length, closePosition);
                const javadoc = [];
                const lines = rawJavadoc.split('\n');
                lines.shift();
                lines.pop();
                for (let line of lines) {
                    // Remove Beginning Tag
                    line = line.trim();
                    const middleTag = '*';
                    if (line.startsWith(middleTag)) {
                        line = line.substring(middleTag.length);
                    }

                    // Handle Empty Lines
                    line = line.trim();
                    if (line.length === 0) {
                        line = '<br>';
                    }

                    // Convert Code Block
                    const codeTag = '`';
                    if (line.startsWith(codeTag) && line.endsWith(codeTag)) {
                        line = line.substring(codeTag.length, line.length - codeTag.length);
                        line = `<pre>${line}</pre>`;
                    }

                    // Add To Final JavaDoc
                    javadoc.push(line);
                }
                // Add JavaDoc To Class
                data.javadoc = javadoc.join('\n');
            }
        }
    }
}