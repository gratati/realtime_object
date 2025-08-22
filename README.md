# 📱 Real-Time Object Detection

[![TensorFlow](https://img.shields.io/badge/TensorFlow-FF6F00?style=for-the-badge&logo=tensorflow&logoColor=white)](https://www.tensorflow.org/lite/examples/object_detection/overview?hl=ru)
[![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)

Приложение для Android, использующее модель TensorFlow Lite для детекции объектов в реальном времени через камеру устройства и при обработке видео из галереи.

## ✨ Возможности

- 🎥 **Детекция в реальном времени** через камеру устройства
- 🖼️ **Визуализация результатов** с наложением рамок и меток
- 📁 **Обработка видео** из галереи устройства
- ⚡ **Высокая производительность** благодаря TensorFlow Lite
- 🎯 **Точное распознавание** множества классов объектов

## 🛠️ Технологический стек

| Компонент         | Технология                     |
|-------------------|--------------------------------|
| Язык программирования | Kotlin                     |
| ML-фреймворк      | TensorFlow Lite 2.3.0+         |
| Работа с камерой  | Android Camera2 API            |
| Среда разработки  | Android Studio 4.1+            |
| Мин. Android SDK | 21 (Android 5.0 Lollipop)      |

## 📋 Требования

- Android Studio 4.1 или новее
- Android SDK 21 или новее
- TensorFlow Lite 2.3.0 или новее
- Устройство с Android 5.0+ и камерой

## 🚀 Установка

1. **Клонируйте репозиторий:**
   ```bash
   git clone https://github.com/yourusername/object-detection-app.git
Откройте проект в Android Studio:
Запустите Android Studio
Выберите "Open an existing project"
Укажите путь к клонированному репозиторию
Синхронизируйте Gradle-файлы:
Дождитесь завершения синхронизации (появится сообщение "Gradle project sync finished")
При необходимости нажмите "Sync Now" в уведомлении
Запустите приложение:
Подключите Android-устройство или запустите эмулятор
Нажмите кнопку Run (▶️) в Android Studio
💡 Использование
Детекция через камеру:
При первом запуске предоставьте разрешение на использование камеры
Наведите камеру на объекты
Наблюдайте за детекцией в реальном времени:
Рамки вокруг обнаруженных объектов
Названия классов и уровень уверенности
Обработка видео из галереи:
Нажмите кнопку выбора видео в интерфейсе приложения
Выберите видеофайл из галереи устройства
Дождитесь завершения обработки
Просмотрите результаты детекции с наложенными метками
📸 Пример работы
Камера в реальном времени
Обработка видео из галереи
Camera Detection
Video Processing
🔧 Модель TensorFlow Lite
Приложение использует предварительно обученную модель для детекции объектов:

Источник: TensorFlow Lite Object Detection Model
Поддерживаемые классы: 80+ категорий (люди, животные, транспорт, бытовые предметы)
Формат: .tflite
Оптимизация: Quantized для мобильных устройств
📝 Лицензия
Этот проект распространяется под лицензией MIT - подробности в файле LICENSE.

🤝 Вклад в проект
Мы приветствуем вклад в развитие проекта! Если вы хотите внести улучшения:

Сделайте форк репозитория
Создайте ветку для вашей функции (git checkout -b feature/AmazingFeature)
Закоммитьте изменения (git commit -m 'Add some AmazingFeature')
Отправьте в репозиторий (git push origin feature/AmazingFeature)
Откройте Pull Request
📬 Контакты
Если у вас возникли вопросы или предложения, свяжитесь:

<div align="center">

Email gratati49@gmail.com
GitHub https://github.com/gratati



