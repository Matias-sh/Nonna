@echo off
echo Deteniendo servidor Django...
taskkill /f /im python.exe 2>nul
timeout /t 2 /nobreak >nul

echo Actualizando configuracion ALLOWED_HOSTS...
echo ALLOWED_HOSTS=localhost,127.0.0.1,10.0.2.2 > .env.temp
echo DEBUG=True >> .env.temp
echo SECRET_KEY=your-secret-key-here >> .env.temp
echo CORS_ALLOWED_ORIGINS=http://localhost:3000,http://127.0.0.1:3000,http://10.0.2.2:8000,http://10.0.2.2:3000 >> .env.temp
move .env.temp .env

echo Iniciando servidor Django...
cd /d "%~dp0"
call venv\Scripts\activate
python manage.py runserver 0.0.0.0:8000
