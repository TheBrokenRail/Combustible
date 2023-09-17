import * as fs from 'node:fs';
import * as child_process from 'node:child_process';
import { classes, NUMBER_TYPE, srcPath } from './common';
import { APIClassInfo } from './api-classes';
import { EnumInfo } from './enums';
import { apiVersion } from './constants';
import { OpenAPIV3 } from 'openapi-types';

// Create Document
const document: OpenAPIV3.Document = {
    openapi: '3.0.0',
    info: {
        title: 'Lemmy API',
        version: child_process.execFileSync('git', ['describe', '--tags', '--dirty'], {cwd: srcPath}).toString().trim()
    },
    components: {
        schemas: {}
    },
    paths: {},
    servers: [
        {
            url: `https://lemmy.world/api/${apiVersion}`
        },
        {
            url: `https://lemmy.ml/api/${apiVersion}`
        },
        {
            url: `https://voyager.lemmy.ml/api/${apiVersion}`
        }
    ]
};

// Type Conversion
function convertJavaTypeToOpenAPI(type: string): 'string' | 'integer' | 'boolean' | 'object' {
    if (type === NUMBER_TYPE) {
        return 'integer';
    } else if (type === 'String') {
        return 'string';
    } else if (type === 'Boolean') {
        return 'boolean';
    } else {
        throw new Error();
    }
}

// Generate Schema For Type
function generateScheme(type: string): OpenAPIV3.SchemaObject | OpenAPIV3.ReferenceObject {
    return classes[type] !== undefined ? {
        $ref: `#/components/schemas/${type}`
    } : {
        type: convertJavaTypeToOpenAPI(type)
    };
}

// Add Objects
for (const className in classes) {
    const classInfo = classes[className];
    if (classInfo === undefined) {
        continue;
    }
    if (classInfo instanceof APIClassInfo) {
        // Class
        const schema: OpenAPIV3.SchemaObject = {
            type: 'object',
            properties: {},
            required: []
        };
        for (const field of classInfo.fields) {
            const listPrefix = 'List<';
            if (field.type.startsWith(listPrefix)) {
                // Array
                const innerType = field.type.substring(listPrefix.length, field.type.length - 1);
                const property: OpenAPIV3.SchemaObject = {
                    type: 'array',
                    items: generateScheme(innerType)
                };
                schema.properties![field.name] = property;
            } else {
                // Normal Object
                schema.properties![field.name] = generateScheme(field.type);
            }
            if (!field.nullable) {
                // Required
                schema.required!.push(field.name);
            }
        }
        if (classInfo.usesToken) {
            // Authentication
            schema.properties!['auth'] = {
                type: 'string',
                description: 'API Token'
            };
            if (classInfo.requiresToken) {
                schema.required!.push('auth');
            }
        }
        if (Object.keys(schema.properties!).length === 0) {
            // https://github.com/OpenAPITools/openapi-generator/issues/7638#issuecomment-1614613756
            schema.allOf = [];
        }
        document.components!.schemas![classInfo.name] = schema;
    } else if (classInfo instanceof EnumInfo) {
        // Enum
        const schema: OpenAPIV3.SchemaObject = {
            type: 'string',
            enum: classInfo.values
        };
        document.components!.schemas![classInfo.name] = schema;
    }
}

// Add API Calls
for (const className in classes) {
    const classInfo = classes[className];
    if (classInfo === undefined) {
        continue;
    }
    if (classInfo instanceof APIClassInfo && classInfo.isMethod) {
        // Build Summary
        let summary = undefined;
        if (classInfo.javadoc !== null) {
            summary = classInfo.javadoc.split('\n')[0];
        }
        // Build Response Schema
        const response: OpenAPIV3.SchemaObject | OpenAPIV3.ReferenceObject = classInfo.responseClassType === 'Object' ? {
            type: 'object',
            properties: {}
        } : {
            $ref: `#/components/schemas/${classInfo.responseClassType}`
        };
        // Build API Call
        if (document.paths![`/${classInfo.path}`] === undefined) {
            document.paths![`/${classInfo.path}`] = {};
        }
        const path = document.paths![`/${classInfo.path}`]!;
        if (classInfo.httpType === 'GET') {
            // GET
            path.get = {
                operationId: classInfo.name,
                parameters: [],
                responses: {
                    '200': {
                        description: 'OK',
                        content: {
                            'application/json': {
                                schema: response
                            }
                        }
                    }
                }
            };
            if (classInfo.fields.length > 0 || classInfo.usesToken) {
                // Query Parameters
                path.get!.parameters!.push({
                    in: 'query',
                    name: classInfo.name,
                    schema: {
                        $ref: `#/components/schemas/${classInfo.name}`
                    },
                    required: true,
                    explode: true
                });
            }
            if (summary !== undefined) {
                // Summary
                path.get!.summary = summary;
            }
        } else {
            // POST/PUT
            const method = classInfo.httpType === 'POST' ? 'post' : 'put';
            const operation: OpenAPIV3.OperationObject = {
                operationId: classInfo.name,
                responses: {
                    '200': {
                        description: 'OK',
                        content: {
                            'application/json': {
                                schema: response
                            }
                        }
                    }
                }
            };
            if (classInfo.fields.length > 0 || classInfo.usesToken) {
                // Request Body
                operation.requestBody = {
                    required: true,
                    content: {
                        'application/json': {
                            schema: {
                                $ref: `#/components/schemas/${classInfo.name}`
                            }
                        }
                    }
                };
            }
            if (summary !== undefined) {
                // Summary
                operation.summary = summary;
            }
            path[method] = operation;
        }
    }
}

// Write
fs.writeFileSync('openapi.json', JSON.stringify(document));