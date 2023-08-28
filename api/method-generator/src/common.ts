// Constants
export const PACKAGE = 'com.thebrokenrail.combustible.api.method';
export const INDENT = '    ';
export const NUMBER_TYPE = 'Integer';

// Source Path
import * as path from 'node:path';
export const rootPath = path.join(__dirname, '..', '..');
export const srcPath = path.join(__dirname, '..', 'lemmy-js-client', 'src');
export const typesPath = path.join(srcPath, 'types');

// Type Info
export interface ClassInfo {
    readonly name: string;
    toString(): string;
}
export const classes: { [name: string]: ClassInfo } = {};

// Utility Class
export class TokenFinder {
    readonly str: string;
    position: number;

    constructor(str: string) {
        this.str = str;
        this.position = 0;
    }

    next(token: string) {
        const newPosition = this.str.indexOf(token, this.position);
        if (newPosition === -1) {
            return null;
        }
        const result = this.str.substring(this.position, newPosition);
        this.position = newPosition + token.length;
        return result;
    }
}