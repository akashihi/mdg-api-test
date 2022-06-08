const pactum = require('pactum');

pactum.handler.addSpecHandler('Create Category', (ctx) => {
  const { spec, data } = ctx;
  spec.post('/category');
  spec.withJson(data);
  spec.use('create');
});

pactum.handler.addSpecHandler('Get Category Tree', (ctx) => {
  const { spec, data } = ctx;
  spec.get('/category/{id}');
  spec.withPathParams('id', data);
  spec.use('read');
});
