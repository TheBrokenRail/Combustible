import * as path from 'node:path';
import * as fs from 'node:fs';
import { PACKAGE, classes, rootPath, typesPath } from './common';

// Create Output Directory
const out = path.join(rootPath, 'src', 'main', 'java', ...PACKAGE.split('.'));
fs.rmSync(out, {recursive: true, force: true});
fs.mkdirSync(out, {recursive: true});

// Load All Type Definitions
let definitions = '';
for (const file of fs.readdirSync(typesPath)) {
    if (file.endsWith('.ts') && file !== 'others.ts') {
        definitions += fs.readFileSync(path.join(typesPath, file), {encoding: 'utf8'});
    }
}

// Load Classes
import { load as loadEnums } from './enums';
loadEnums(definitions);
import { load as loadApiClasses } from './api-classes';
loadApiClasses(definitions);
import './constants';

// Write Classes
for (const className in classes) {
    const classInfo = classes[className];
    if (classInfo === undefined) {
        continue;
    }
    const result = classInfo.toString();
    fs.writeFileSync(path.join(out, `${className}.java`), result);
}