const { addSpecHandler } = require('pactum').handler;

addSpecHandler('expect error', (ctx) => {
    const { spec, data } = ctx;
    const { statusCode, title, instance } = data;
    spec.withHeaders('Content-Type', 'application/vnd.mdg+json;version=1');
    spec.expectStatus(statusCode);
    spec.expectHeader('content-type', 'application/vnd.mdg+json;version=1');
    spec.expectJson('status', statusCode);
    spec.expectJson('title', title);
    if (instance) {
        spec.expectJson('instance', instance);
    }
});
