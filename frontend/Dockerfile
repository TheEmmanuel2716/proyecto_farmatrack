# ---- build ----
FROM docker.io/library/node:22-alpine AS build
WORKDIR /app
RUN npm install -g pnpm@10.28.0

COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .
RUN pnpm build

# ---- runtime ----
FROM docker.io/library/nginx:1.27-alpine
ENV GATEWAY_URL=http://graphql-gateway:8083
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
