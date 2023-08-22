// Modules
const path = require('node:path');
const fs = require('node:fs');

// Constants
const PACKAGE = 'com.thebrokenrail.combustible.api.method';
const INDENT = '    ';
const NUMBER_TYPE = 'Integer';

// Create Output Directory
const out = path.join(__dirname, 'src', 'main', 'java', ...PACKAGE.split('.'));
fs.rmSync(out, {recursive: true, force: true});
fs.mkdirSync(out, {recursive: true});

// Load All Type Definitions
let definitions = '';
const srcPath = path.join(__dirname, 'lemmy-js-client', 'src');
const typesPath = path.join(srcPath, 'types');
for (const file of fs.readdirSync(typesPath)) {
    if (file.endsWith('.ts') && file !== 'others.ts') {
        definitions += fs.readFileSync(path.join(typesPath, file), {encoding: 'utf8'});
    }
}

// Fix Type
function fixType(type) {
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

// Locate Classes
const usesToken = [];
const requiresToken = [];
const classes = {};
let classData = null;
let className = null;
for (let line of definitions.split('\n')) {
    line = line.trim();
    if (line === '}' && className !== null) {
        // End Class
        classData += '}\n';
        classes[className] = classData;
        classData = null;
        className = null;
    } else if (line.startsWith('export interface ') && className === null) {
        // Start Class
        className = line.split(' ')[2];
        classData = `public class ${className} {\n`;
    } else if (className !== null && !line.startsWith('/**') && !line.startsWith('*') && !line.startsWith('*/')) {
        // Read Class Property
        const nullable = line.indexOf('?:') !== -1;
        line = line.replace(/;/g, '');
        line = line.replace(/\?/g, '');
        let parts = line.split(': ');
        const type = fixType(parts[1]);
        const name = parts[0];
        if (name === 'auth') {
            // Authentication Token Is Treated Separately
            usesToken.push(className);
            if (!nullable) {
                requiresToken.push(className);
            }
        } else {
            classData += INDENT + (nullable ? '@Nullable' : '@NotNull') + '\n';
            classData += INDENT + `public ${type} ${name};\n`;
        }
    }
}

// Utility Class
class TokenFinder {
    constructor(str) {
        this.str = str;
        this.position = 0;
    }
    
    next(token) {
        const newPosition = this.str.indexOf(token, this.position);
        if (newPosition === -1) {
            return null;
        }
        const result = this.str.substring(this.position, newPosition);
        this.position = newPosition + token.length;
        return result;
    }
}

// Locate Enums
let finder = new TokenFinder(definitions);
while (true) {
    // Search
    let str = finder.next('export type ');
    if (str === null) {
        break;
    }
    
    // Find Name
    const name = finder.next(' =');
    
    // Extract Statement
    const statement = finder.next(';');
    
    // Check Statement
    if (!statement.includes('| "') && !statement.includes('" |')) {
        // Not An Enum
        continue;
    }
    
    // Write Java Enum
    classData = `public enum ${name} {\n`;
    for (let piece of statement.split('|')) {
        piece = piece.trim();
        if (piece.length > 0) {
            const value = JSON.parse(piece.trim());
            classData += INDENT + value + ',\n';
        }
    }
    classData += '}\n';
    classData = classData.replace(/,\n\}/g, '\n}');
    classes[name] = classData;
}

// Read http.ts
const httpTs = fs.readFileSync(path.join(srcPath, 'http.ts'), {encoding: 'utf8'});

// Read Type Information From http.ts
finder = new TokenFinder(httpTs);
while (true) {
    // Search
    let str = finder.next('this.#wrapper<');
    if (str === null) {
        break;
    }
    
    // Find Types
    let piece = finder.next('>')
    piece = piece.replace(/ /g, '');
    piece = piece.replace(/\n/g, '');
    piece = piece.split(',');
    const requestClassType = piece[0];
    let responseClassType = piece[1];
    if (!classes[responseClassType]) {
        responseClassType = 'Object';
    }
    
    // Find HTTP Type
    finder.next('HttpType.');
    const httpType = finder.next(',').toUpperCase();
    
    // Find Path
    finder.next('"/');
    const path = finder.next('"');
    
    // Add Information To Class
    let data = classes[requestClassType];
    const base = usesToken.indexOf(requestClassType) !== -1 ? 'Connection.AuthenticatedMethod' : 'Connection.Method';
    data = data.replace(/ \{\n/g, ` extends ${base}<${responseClassType}> {\n`);
    data = data.replace(/\}\n/g, '');
    data += INDENT + '@Override\n';
    data += INDENT + `public Class<${responseClassType}> getResponseClass() {\n`;
    data += INDENT + INDENT + `return ${responseClassType}.class;\n`;
    data += INDENT + '}\n';
    data += INDENT + '@Override\n';
    data += INDENT + 'public String getPath() {\n';
    data += INDENT + INDENT + `return "${path}";\n`;
    data += INDENT + '}\n';
    data += INDENT + '@Override\n';
    data += INDENT + 'public Type getType() {\n';
    data += INDENT + INDENT + `return Type.${httpType};\n`;
    data += INDENT + '}\n';
    if (requiresToken.indexOf(requestClassType) !== -1) {
        data += INDENT + '@Override\n';
        data += INDENT + 'public boolean requiresToken() {\n';
        data += INDENT + INDENT + 'return true;\n';
        data += INDENT + '}\n';
    }
    data += '}\n';
    classes[requestClassType] = data;
}

// Write Classes
for (const className in classes) {
    const classData = classes[className];
    let result = `package ${PACKAGE};\n`;
    const imports = [];
    if (classData.includes('extends Connection')) {
        imports.push('com.thebrokenrail.combustible.api.Connection');
    }
    if (classData.includes('@NotNull')) {
        imports.push('org.jetbrains.annotations.NotNull');
    }
    if (classData.includes('@Nullable')) {
        imports.push('org.jetbrains.annotations.Nullable');
    }
    if (classData.includes('List<')) {
        imports.push('java.util.List');
    }
    if (imports.length > 0) {
        result += '\n';
    }
    for (const str of imports) {
        result += `import ${str};\n`;
    }
    result += '\n';
    result += classData;
    fs.writeFileSync(path.join(out, `${className}.java`), result);
}
