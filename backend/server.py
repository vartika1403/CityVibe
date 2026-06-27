"""
CityVibe ingress proxy.

The real REST API is implemented in Spring Boot (Java) and runs on localhost:8090
(see /app/backend-springboot). The Emergent platform ingress only exposes port 8001,
so this lightweight FastAPI app transparently forwards every /api/* request to the
Spring Boot service. All business logic, persistence (MySQL) and data live in Spring Boot.
"""
import os
import httpx
from fastapi import FastAPI, Request, Response
from starlette.middleware.cors import CORSMiddleware

SPRING_BOOT_URL = os.environ.get("SPRING_BOOT_URL", "http://localhost:8090")

app = FastAPI(title="CityVibe Ingress Proxy")

app.add_middleware(
    CORSMiddleware,
    allow_credentials=True,
    allow_origins=os.environ.get("CORS_ORIGINS", "*").split(","),
    allow_methods=["*"],
    allow_headers=["*"],
)

client = httpx.AsyncClient(base_url=SPRING_BOOT_URL, timeout=30.0)


@app.get("/api/health")
async def health() -> dict[str, object]:
    try:
        r = await client.get("/api/events")
        return {"status": "ok", "spring_boot": "up", "events": len(r.json())}
    except Exception as e:  # noqa: BLE001
        return {"status": "degraded", "spring_boot": "down", "error": str(e)}


@app.api_route(
    "/api/{path:path}",
    methods=["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"],
)
async def proxy(path: str, request: Request) -> Response:
    url: str = f"/api/{path}"
    body = await request.body()
    headers = {k: v for k, v in request.headers.items() if k.lower() != "host"}
    upstream = await client.request(
        request.method,
        url,
        params=request.query_params,
        content=body,
        headers=headers,
    )
    excluded = {"content-encoding", "transfer-encoding", "connection", "content-length"}
    resp_headers = {k: v for k, v in upstream.headers.items() if k.lower() not in excluded}
    return Response(
        content=upstream.content,
        status_code=upstream.status_code,
        headers=resp_headers,
        media_type=upstream.headers.get("content-type"),
    )


@app.on_event("shutdown")
async def shutdown():
    await client.aclose()
