ALTER TABLE veiculos
ADD CONSTRAINT fk_veiculos_categoria
FOREIGN KEY (categoria_id)
REFERENCES categorias_veiculos(id);