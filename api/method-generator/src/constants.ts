import * as path from 'node:path';
import * as fs from 'node:fs';

import { PACKAGE, INDENT, ClassInfo, classes, typesPath } from './common';

// Find API Version
export let apiVersion = '';
const othersTs = fs.readFileSync(path.join(typesPath, 'others.ts'), {encoding: 'utf8'});
for (const line of othersTs.split('\n')) {
    if (line.startsWith('export const VERSION = "')) {
        // Found Version
        const parts = line.split(' ');
        // Extract Version
        let version = parts[4];
        if (version === undefined) {
            break;
        }
        // Remove Semicolon And Qutes
        version = version.substring(1, version.length - 2);
        apiVersion = version;
    }
}

// Constants Information
class ConstantsInfo implements ClassInfo {
    readonly name: string = 'Constants';

    toString() {
        let data = '';

        // Package
        data += `package ${PACKAGE};\n\n`;

        // JavaDoc
        data += '/**\n * API constants.\n */\n';

        // Open Class
        data += `public class ${this.name} {\n`;

        // API Version
        data += INDENT + '/**\n';
        data += INDENT + ' * API version.\n';
        data += INDENT + ' */\n';
        data += INDENT + `public static final String VERSION = "${apiVersion}";\n`;

        // Close Class
        data += '}\n';

        // Return
        return data;
    }
}
const constantsInfo = new ConstantsInfo();
classes[constantsInfo.name] = constantsInfo;