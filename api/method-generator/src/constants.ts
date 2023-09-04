import * as path from 'node:path';
import * as fs from 'node:fs';

import { PACKAGE, INDENT, ClassInfo, classes, typesPath } from './common';

// Constants Information
class ConstantsInfo implements ClassInfo {
    readonly name: string = 'Constants';

    toString() {
        let data = '';

        // Package
        data += `package ${PACKAGE};\n\n`;

        // Open Class
        data += `public class ${this.name} {\n`;

        // API Version
        const othersTs = fs.readFileSync(path.join(typesPath, 'others.ts'), {encoding: 'utf8'});
        for (const line of othersTs.split('\n')) {
            if (line.startsWith('export const VERSION = "')) {
                // Found Version
                const parts = line.split(' ');
                // Extract Version
                const version = parts[4];
                if (version === undefined) {
                    break;
                }
                // Write
                data += INDENT + `public static final String VERSION = ${version}\n`;
            }
        }

        // Close Class
        data += '}\n';

        // Return
        return data;
    }
}
const constantsInfo = new ConstantsInfo();
classes[constantsInfo.name] = constantsInfo;