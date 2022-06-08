const pactum = require('pactum');

pactum.handler.addSpecHandler('Create Account', (ctx) => {
    const { spec, data } = ctx;
    spec.post('/accounts');
    spec.withJson(data);
    spec.use('create');
});

pactum.handler.addSpecHandler('Validate account balance', (ctx) => {
    const { spec, data } = ctx;
    const { id, balance } = data;
    spec.use('read');
    spec.get('/accounts/{id}');
    spec.withPathParams('id', id);
    spec.expectJson('balance', balance);
});
