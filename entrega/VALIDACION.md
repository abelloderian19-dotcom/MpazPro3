# APK firmado — MPAZ PRO 3 1.0.1

- Aplicación: `MPAZPRO3-1.0.1-release.apk`
- Versión Android: `1.0.1` (`versionCode` 2)
- Firma comprobada con Android SDK Build Tools 36.0.0 y `apksigner`.
- Esquemas comprobados: V1 (JAR) y V2 (APK Signature Scheme).
- Certificado público: `MPAZPRO3-certificado-publico.der`
- Huella SHA-256 del certificado: `273b640a279e3dd7bf1081d9ef48117be1ca7276e63bea6fbd1e98021e8e1b19`
- SHA-256 del APK: `43bc3483d2f6d4f2b720cfbb9c08c3c54cf956646cd76a4f3ebb5b138d9f40c7`
- Pruebas JVM: 169 casos descubiertos, 0 fallos y 47 casos REST ignorados; JaCoCo mide 99,5% de líneas cubiertas en `domain/` (mínimo exigido: 80%).
- Prueba UI: 3 de 3 casos pasaron en Android 17/API 37 con Espresso 3.7.0.

Para repetir la verificación en Windows:

```powershell
apksigner verify --verbose --min-sdk-version 18 --print-certs .\MPAZPRO3-1.0.1-release.apk
```

La llave `.jks` y `keystore.properties` no se publican en Git. La llave de evaluación se guarda localmente y se entrega al docente por el canal privado que indique.
