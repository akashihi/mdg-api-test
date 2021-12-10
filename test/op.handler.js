const { addSpecHandler } = require('pactum').handler;

addSpecHandler('read', (ctx) => {
    const { spec, data } = ctx;
    spec.expectStatus(200);
    spec.expectHeader('content-type', 'application/vnd.mdg+json')
});

addSpecHandler('modification', (ctx) => {
    const { spec, data } = ctx;
    spec.expectStatus(202);
    spec.expectHeader('content-type', 'application/vnd.mdg+json')
});