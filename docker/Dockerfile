# Imagem ultra-leve Nginx Alpine para servir o Compose WebAssembly no Synology NAS
FROM nginx:alpine

# Copiar a configuração do Nginx com suporte a MIME type .wasm
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiar os ficheiros estáticos gerados pela compilação WasmJs (./gradlew :composeApp:wasmJsBrowserDistribution)
# Diretório de destino padrão do Nginx
COPY ./dist/ /usr/share/nginx/html/

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
