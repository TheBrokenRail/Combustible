import { PACKAGE, INDENT, ClassInfo, classes, TokenFinder } from './common';

// Enum Information
export class EnumInfo implements ClassInfo {
    readonly name: string;
    readonly values: string[];

    constructor(name: string) {
        this.name = name;
        this.values = [];
    }

    toString() {
        let data = '';

        // Package
        data += `package ${PACKAGE};\n\n`;

        // Suppress Warning If Needed
        data += '@SuppressWarnings({"unused", "RedundantSuppression"})\n';

        // Open Enum
        data += `public enum ${this.name} {\n`;

        // Fields
        for (const value of this.values) {
            data += INDENT + value + ',\n';
        }

        // Close Enum
        data += '}\n';

        // Remove Last Comma
        data = data.replace(/,\n\}/g, '\n}');

        // Return
        return data;
    }
}

// Load Function
export function load(definitions: string) {
    // Locate Enums
    const finder = new TokenFinder(definitions);
    while (true) {
        // Search
        const str = finder.next('export type ');
        if (str === null) {
            break;
        }

        // Find Name
        const name = finder.next(' =');
        if (name === null) {
            break;
        }
        const enumInfo = new EnumInfo(name);

        // Extract Statement
        const statement = finder.next(';');
        if (statement === null) {
            break;
        }

        // Check Statement
        if (!statement.includes('| "') && !statement.includes('" |')) {
            // Not An Enum
            continue;
        }

        // Write Java Enum
        for (let piece of statement.split('|')) {
            piece = piece.trim();
            if (piece.length > 0) {
                const value = JSON.parse(piece.trim());
                enumInfo.values.push(value);
            }
        }

        // Store Enum
        classes[enumInfo.name] = enumInfo;
    }
}