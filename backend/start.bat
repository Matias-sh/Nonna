@echo off
echo 🚀 Iniciando backend de Nonna...

REM Activar entorno virtual
echo 🔧 Activando entorno virtual...
call venv\Scripts\activate.bat

REM Instalar dependencias
echo 📥 Instalando dependencias...
pip install -r requirements.txt

REM Iniciar servidor
echo 🌐 Iniciando servidor...
echo 📱 Backend disponible en: http://localhost:8000
echo 🔗 API disponible en: http://localhost:8000/api/
echo.
echo 👤 Credenciales de admin:
echo    Email: admin@nonna.com
echo    Password: admin123
echo.
echo 🛑 Presiona Ctrl+C para detener el servidor
echo.

python manage.py runserver 0.0.0.0:8000