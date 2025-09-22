@echo off
echo 🚀 Configurando backend de Nonna por primera vez...

REM Crear entorno virtual
echo 📦 Creando entorno virtual...
python -m venv venv

REM Activar entorno virtual
echo 🔧 Activando entorno virtual...
call venv\Scripts\activate.bat

REM Actualizar pip y setuptools
echo 🔄 Actualizando pip y setuptools...
python -m pip install --upgrade pip setuptools wheel

REM Instalar dependencias
echo 📥 Instalando dependencias...
pip install -r requirements.txt

REM Crear archivo .env
echo ⚙️  Creando archivo de configuración...
echo ALLOWED_HOSTS=localhost,127.0.0.1,10.0.2.2 > .env
echo DEBUG=True >> .env
echo SECRET_KEY=django-insecure-change-this-in-production >> .env
echo CORS_ALLOWED_ORIGINS=http://localhost:3000,http://127.0.0.1:3000,http://10.0.2.2:8000,http://10.0.2.2:3000 >> .env

REM Ejecutar migraciones
echo 🗄️  Ejecutando migraciones...
python manage.py makemigrations
python manage.py migrate

REM Crear superusuario
echo 👤 Creando superusuario...
python manage.py shell -c "from django.contrib.auth import get_user_model; User = get_user_model(); User.objects.create_superuser('admin@nonna.com', 'admin', 'Administrador', 'admin123') if not User.objects.filter(email='admin@nonna.com').exists() else print('Superusuario ya existe')"

echo ✅ Configuración completada!
echo.
echo Para iniciar el servidor, ejecuta: start.bat
echo.
pause
