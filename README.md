# Sprint Spirit

Sprint Spirit es una aplicación para dispositivos móviles creada con el objetivo de brindar
a entusiastas del senderismo una plataforma común donde registrar y compartir sus rutas.
Las principales características del software son:

- **Grabación de rutas**: La principal característica es la posibilidad de grabar trayectos
sin necesidad de una conexión a Internet.
- **Registro de rutas**: Facilita mantener un historial detallado de todas las rutas reali-
zadas, incluyendo estadísticas como distanciao tiempo de recorrido por cada una
de ellas.
- **Compartir rutas**: Posibilita compartir rutas completadas con otros usuarios de la
comunidad Sprint Spirit, fomentando el intercambio de experiencias y descubri-
mientos.
- **Discusión de rutas**: Permite comentar y discutir sobre rutas específicas, proporcio-
nando un espacio para recomendaciones y consejos entre los usuarios.
- **Moderación**: Para mantener la comunidad sana, usuarios con rol de administrador
pueden moderar el contenido que no cumpla con las reglas.
- **Más aspectos sociales**: Incluye funciones sociales adicionales como seguir a otros
usuarios o buscar rutas por lugar de salida.

### Uso

1. Descargue la APK en releases.
2. Instale la app en un dispositivo con Android 10 o superior.
3. Regístrese.

### Set-up del backend

Aunque la mayor parte del backend está construido con los servicios de Firebase, la comunicación
con las notificaciones no. Para solventarlo, asegúrese de que tiene Python3 instalado en su equipo
y siga los siguientes pasos: 

1. `git clone https://github.com/KarimRuiz/Sprint-Spirit.git`
2. Sitúese en el directorio backend: `cd ./backend`
3. Ejecute ChatsManager.py: `python3 ChatsManager.py`

Si no realiza este paso, las notificaciones no llegarán a ningún dispositivo, pero el resto de funciones
de los chats seguirán funcionando correctamente.

### Capturas

![Inicio](https://i.imgur.com/K545B2y.jpeg)
![Filtros](https://i.imgur.com/c6FQw4V.jpeg)

![Perfil](https://i.imgur.com/gHaxdqm.jpeg)

![Grabación de una ruta](https://i.imgur.com/JM6mhty.jpeg)

![Chats](https://i.imgur.com/ognc4M3.jpegg)