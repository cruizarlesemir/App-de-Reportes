# Reportes App (Android + API en Render)

Aplicación Android en **Kotlin + Jetpack Compose** que consume el servicio publicado en:

```
https://reporteservice-9fdr.onrender.com/
```

## Funcionalidades

- Lista paginada de reportes (5 por página) con botón **"Cargar 5 más"**.
- Formulario para registrar un reporte (DNI, tipo, dirección, referencia y fecha automática).
- Pantalla de detalle con botón **"Atender reporte"** (cambia `estado` de 1 a 0).
- Búsqueda de reportes por **DNI** del ciudadano.
- Mensajes de error claros y tolerancia al "cold start" de Render (timeouts largos).

## 1. Configuración de la API

Ya está configurada en:

```
app/src/main/java/com/capunay/reportesapp/data/ApiConfig.kt
```

```kotlin
const val BASE_URL_RENDER = "https://reporteservice-9fdr.onrender.com/"
const val BASE_URL = BASE_URL_RENDER
```

Si quieres usar la API local, cambia solo `BASE_URL`:

```kotlin
// Emulador
const val BASE_URL = BASE_URL_EMULADOR   // http://10.0.2.2:8080/

// Celular físico (IP de tu PC en la misma red)
const val BASE_URL = BASE_URL_CELULAR    // http://192.168.1.20:8080/
```

## 2. Endpoints reales del servicio

| Acción | Método y ruta | Uso en la app |
|---|---|---|
| Registrar | `POST reportes/` | Formulario |
| Listar paginado | `GET reportes/?offset=0&limit=5` | Lista + "Cargar 5 más" |
| Obtener uno | `GET reportes/{id}` | Pantalla de detalle |
| Reportes de un ciudadano | `GET reportes/ciudadano/{dni}` | Búsqueda por DNI |
| Atender | `PUT reportes/{id}` | Botón "Atender reporte" |

La paginación es por **offset**: la app empieza en `offset=0`, luego pide `offset=5`, `offset=10`, etc.
Para saber si hay más páginas usa `hasNext` / `totalElements` de la respuesta.

Respuesta del listado:

```json
{
  "content": [
    {
      "id": 1,
      "dni": "12345678",
      "fecha": "2026-10-29T10:15:30",
      "tipo": "MALEZA",
      "direccion": "Av. Ejemplo 123",
      "referencia": "Cerca al parque",
      "estado": 1
    }
  ],
  "offset": 0,
  "limit": 5,
  "totalElements": 1,
  "totalPages": 1,
  "hasNext": false
}
```

Cuerpo que envía al registrar (la API inicializa `estado` en 1):

```json
{
  "dni": "12345678",
  "fecha": "2026-10-29T10:15:30",
  "tipo": "MALEZA",
  "direccion": "Av. Ejemplo 123",
  "referencia": "Cerca al parque"
}
```

> Si el backend exige la fecha como `yyyy-MM-dd HH:mm:ss`, la app reintenta automáticamente
> con ese formato cuando recibe un HTTP 400.

La búsqueda por DNI devuelve un array:

```json
[ { "id": 1, "dni": "12345678", "...": "..." } ]
```

## 3. Ejecutar

1. Abre el proyecto en Android Studio.
2. Espera la sincronización de Gradle.
3. Ejecuta en un emulador o celular (`Run ▶`).

APK generado: `app/build/outputs/apk/debug/app-debug.apk`

### Emulador
- Usa la URL de Render o `http://10.0.2.2:8080/` para una API local.

### Celular físico
- PC y celular en la misma red WiFi.
- Cambia a `BASE_URL_CELULAR` con la IP de tu PC.
- Permite el puerto 8080 en el firewall de Windows (solo si usas API local).

## 4. Permisos configurados

En `app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
...
<application
    android:usesCleartextTraffic="true"
    ... >
```

`usesCleartextTraffic` permite HTTP local; Render usa HTTPS.

## 5. Probar la API antes de la app

```powershell
Invoke-RestMethod "https://reporteservice-9fdr.onrender.com/reportes/?offset=0&limit=5" | ConvertTo-Json -Depth 5
```

```powershell
Invoke-RestMethod "https://reporteservice-9fdr.onrender.com/reportes/ciudadano/12345678" | ConvertTo-Json -Depth 5
```

## 6. Render: primera petición lenta

El plan gratuito suspende el servicio. La primera petición puede tardar cerca de un minuto.
El cliente HTTP ya está configurado con:

- `connectTimeout`: 60 s
- `readTimeout`: 120 s
- `callTimeout`: 180 s

Si la primera vez falla por timeout, toca **Reintentar**.

## 7. Estructura del código

```
app/src/main/java/com/capunay/reportesapp/
├── MainActivity.kt                 # Navegación entre pantallas
├── data/
│   ├── ApiConfig.kt                # URL y rutas
│   ├── ReportesRepository.kt       # Llamadas a la API
│   ├── Errores.kt                  # Mensajes de error amigables
│   ├── model/
│   │   ├── Reporte.kt              # Reporte + NuevoReporte
│   │   └── ReportesPage.kt         # Parser de páginas (content/hasNext)
│   └── remote/
│       ├── ReportesApi.kt          # Interfaz Retrofit
│       └── ApiClient.kt            # OkHttp + timeouts + Gson
├── ui/
│   ├── ReportesViewModel.kt        # Estado y lógica (offset, búsqueda, alta)
│   ├── components/Componentes.kt   # Chip de estado, tarjetas, etc.
│   ├── screens/
│   │   ├── ListaReportesScreen.kt  # Lista, búsqueda y paginación
│   │   ├── FormularioReporteScreen.kt
│   │   └── DetalleReporteScreen.kt
│   └── theme/
└── util/
    └── Fechas.kt                   # Formatos de fecha tolerantes
```

## 8. Checklist del enunciado

- [x] Package `com.capunay.reportesapp`.
- [x] Consumo de API con Retrofit.
- [x] Lista paginada con botón para cargar los 5 siguientes.
- [x] Formulario de registro.
- [x] Detalle con botón para atender.
- [x] Búsqueda por DNI.
- [x] Permiso de internet y `usesCleartextTraffic`.
- [x] Timeouts para Render.
