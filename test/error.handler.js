const { addSpecHandler } = require('pactum').handler;

addSpecHandler('expect error', (ctx) => {
    const { spec, data } = ctx;
    const { status_code, error_code } = data;
    spec.expectStatus(status_code);
    spec.expectHeader('content-type', 'application/vnd.mdg+json')
    spec.expectJson('errors[0].code', error_code);
});