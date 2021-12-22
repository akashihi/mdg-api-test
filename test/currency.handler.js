const pactum = require('pactum');

pactum.handler.addSpecHandler('Get Currency', (ctx) => {
  const { spec, data } = ctx;
  spec.get('/currency/{id}');
  spec.withPathParams('id', data);
  spec.use('read');
});

pactum.handler.addSpecHandler('Modify Currency', (ctx) => {
  const { spec, data } = ctx;
  const { id, currency } = data;
  spec.put('/currency/{id}');
  spec.withPathParams('id', id);
  spec.withJson(currency);
  spec.use('update');
});
