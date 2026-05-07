# ---- 빌드 단계 ----
FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json .npmrc ./
RUN npm ci

COPY . .

RUN npm run build


# ---- 런타임 단계 ----
FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=10s \
  CMD wget --quiet --tries=1 --spider http://127.0.0.1/ || exit 1
