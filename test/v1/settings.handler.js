const pactum = require('pactum');

pactum.handler.addSpecHandler('Set setting value', (ctx) => {
    const { spec, data } = ctx;
    const { id, value } = data;
    spec.put('/settings/{id}');
    spec.withPathParams('id', id);
    spec.withJson({
        id: id,
        value: value
    });
    spec.expectJson('value', value);
    spec.use('update');
});

pactum.handler.addSpecHandler('Check setting value', (ctx) => {
    const { spec, data } = ctx;
    const { id, value } = data;
    spec.get('/settings/{id}');
    spec.withPathParams('id', id);
    spec.expectJson('value', value);
    spec.use('read');
});
