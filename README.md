# Mi Radio Yopal

¡Bienvenido a Mi Radio Yopal! Esta aplicación para Android te permite sintonizar las emisoras de radio de Yopal mediante URLs de audio por internet. La aplicación está desarrollada en Kotlin usando Android Studio y se encuentra disponible en [Google Play Store](https://play.google.com/store/apps/details?id=com.terfess.miradioyopal).

## Características

- **Sintonización de Emisoras**: Escucha tus emisoras favoritas agregadas mediante URLs de audio.
- **Reproducción en Segundo Plano**: La aplicación mantiene la reproducción de audio incluso cuando sales de la aplicación, gracias a su servicio en primer plano.
- **ExoPlayer3**: Utiliza la librería ExoPlayer3 para una reproducción de audio eficiente y fiable.
- **Almacenamiento Local**: Guarda las emisoras y otros datos localmente usando SQLite.
- **Integración con Firebase Firestore**: Obtiene datos de emisoras y otra información relevante desde Firebase Firestore.

## Instalación

1. Clona el repositorio:
    ```bash
    git clone https://github.com/tuusuario/mi-radio-yopal.git
    ```
2. Abre el proyecto en Android Studio.
3. Asegúrate de tener configuradas las claves de Firebase en el archivo `google-services.json`.
4. Compila y ejecuta la aplicación en tu dispositivo o emulador Android.

## Uso
**IMPORTANTE**: Para que la app funcione debe agregarse su archivo google-services.json que proporciona firebase al crear Firestore que es lo que se usa aqui.

1. **Agregar Emisoras**: Las emisoras se pueden agregar mediante URLs de audio desde la interfaz de la aplicación.
2. **Reproducción**: Selecciona una emisora de la lista para empezar a escuchar.
3. **Control de Reproducción**: Puedes pausar, reproducir y cambiar de emisora directamente desde la notificación del sistema, gracias al servicio de sonido en primer plano.

## Tecnologías Utilizadas

- **Lenguaje de Programación**: Kotlin
- **IDE**: Android Studio
- **Librería de Reproducción de Audio**: ExoPlayer3
- **Base de Datos Local**: SQLite
- **Backend**: Firebase Firestore

## Contribuciones

¡Las contribuciones son bienvenidas! Si deseas contribuir, por favor sigue los siguientes pasos:

1. Haz un fork del proyecto.
2. Crea una nueva rama (`git checkout -b feature/nueva-caracteristica`).
3. Realiza tus cambios y haz commit (`git commit -m 'Agregar nueva característica'`).
4. Sube tus cambios al repositorio (`git push origin feature/nueva-caracteristica`).
5. Abre un Pull Request.

## Licencia

Este proyecto está licenciado bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## Soporte/Contacto

Para obtener ayuda o reportar problemas, por favor contacta a [santiagofontalvo13@gmail.com](mailto:santiagofontalvo13@gmail.com).

---

¡Gracias por usar Mi Radio Yopal!

[Descarga Mi Radio Yopal en Google Play Store](https://play.google.com/store/apps/details?id=com.terfess.miradioyopal)
