const { addSpecHandler } = require('pactum').handler;

addSpecHandler('expect success', (ctx) => {
    const { spec, data } = ctx;
    spec.expectStatus(data);
    spec.expectHeader('content-type', 'application/vnd.mdg+json')
});

addSpecHandler('read', (ctx) => {
    const { spec } = ctx;
    spec.use("expect success", 200);
});

addSpecHandler('modification', (ctx) => {
    const { spec } = ctx;
    spec.use("expect success", 202);
});