# Configuration Space Processing project

Utilities for generating and visualizing a configuration space with 3D obstacles to test pathfinding algorithms.
Educational project for BMSTU students.

About configuration spaces:

* [Wikipedia](https://en.wikipedia.org/wiki/C-space)   
* [Introduction on Coursera](https://www.coursera.org/learn/robotics-motion-planning/lecture/0auId/2-1-introduction-to-configuration-space)


## Генератор и визуализатор трехмерных конфигурационных пространств

Проект по разработке генератора и визуализатора конфигурационных пространств для тестирования алгоритмов поиска пути в трехмерном пространстве.
Сгенерированные конфигурационные пространства могут служить заданиями для лабораторных работ по курсу "Автоматизация технологического проектирования".


### Общая информации

Каждое сгенерированное пространсто представляет собой куб со стороной заданной длины, стартовой и конечной точками и множеством (в том числе пустым) препятствий внутри.
Препятствия и внешнее пространсво вокруг куба соответсвуют запретной области, вхождение точки в нее во время поиска пути недопустимо.

Входными данными для генератора служат:

* длина одной стороны  
* заполненность
* зерно инициализации генератора случайных чисел
* имя файла для записи сгененрированного пространства в формате JSON

Визуализатор позволяет отобразить сгенерированное конфигурационное пространство и проверить пересечения траектории точки (пути) с препятствиями.


### Инструкции и документация

* [Генерация конфигурационного пространства](docs/generator.md)
* [Визуализация и проверка пути](docs/visualizer.md)
* [Форматы данных](docs/formats.md)

Назначение директорий проекта:

* `docs` - файлы документации
* `generator` - файлы генератора
* `visualizer` - файлы визуализатора

___

***Комментарии, замечания и предложения приветствуются.***

Для обратной связи можно использовать Issues.
