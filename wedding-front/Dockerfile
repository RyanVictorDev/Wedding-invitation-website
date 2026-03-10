FROM node:20-alpine AS build

WORKDIR /app

# Copia todo o projeto antes do npm install porque o "quasar prepare"
# (postinstall) precisa dos arquivos de config e da estrutura completa
COPY . .

RUN \
  if [ -f package-lock.json ]; then npm ci; \
  else npm install; \
  fi && \
  npm run build

FROM node:20-alpine

WORKDIR /app

RUN npm install -g serve

COPY --from=build /app/dist/spa ./dist

ENV PORT=9000

EXPOSE 9000

CMD ["serve", "-s", "dist", "-l", "9000"]

