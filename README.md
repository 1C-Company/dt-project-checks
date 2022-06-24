[![Build](https://github.com/1C-Company/dt-project-checks/workflows/CI/badge.svg)](https://github.com/1C-Company/dt-project-checks/actions)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=1C-Company_dt-project-checks&metric=coverage)](https://sonarcloud.io/dashboard?id=1C-Company_dt-project-checks)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=1C-Company_dt-project-checks&metric=ncloc)](https://sonarcloud.io/dashboard?id=1C-Company_dt-project-checks)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=1C-Company_dt-project-checks&metric=bugs)](https://sonarcloud.io/dashboard?id=1C-Company_dt-project-checks)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=1C-Company_dt-project-checks&metric=code_smells)](https://sonarcloud.io/dashboard?id=1C-Company_dt-project-checks)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=1C-Company_dt-project-checks&metric=sqale_index)](https://sonarcloud.io/dashboard?id=1C-Company_dt-project-checks)

# 1С:EDT Project checks

Расширение для 1C:EDT, которое проверяет структурную целостность проектов "1С:Предприятия 8".

Проверки по стилю кодирования, логическим ошибкам кода и метаданных, разработка по стандартам 1С располагаются в проекте: https://github.com/1C-Company/v8-code-style/

## Установка

> **Внимание!** Расширение включается в дистрибутив 1C:EDT и не требует дополнительной установки.


Плагин `1С:EDT Project checks` поставляется в виде репозитория `Eclipse`. Ручная установка расширения может выполняться следующими способами:

- непосредственно из p2-репозитория, опубликованного на серверах `фирмы 1С`.
- из локальной копии p2-репозитория, распакованного в локальную папку из предварительно скачанного zip-архива.

В строку выбора репозитория  для установки (`Work with`) вставьте адрес репозитория:

| Версия | P2-репозиторий | ZIP-архив репозитория |
|--------|----------------|-----------------------|
| 0.3.0 для 1C:EDT 2022.2 | https://edt.1c.ru/downloads/releases/plugins/dt-project-checks/edt-2022.2/0.3.0/repo/ | https://edt.1c.ru/downloads/releases/plugins/dt-project-checks/edt-2022.2/0.3.0/repo.zip |
| 0.2.0 для 1C:EDT 2022.1 | https://edt.1c.ru/downloads/releases/plugins/dt-project-checks/edt-2022.1/0.2.0/repo/ | https://edt.1c.ru/downloads/releases/plugins/dt-project-checks/edt-2022.1/0.2.0/repo.zip |
| 0.1.0 для 1C:EDT 2021.3 | https://edt.1c.ru/downloads/releases/plugins/dt-project-checks/edt-2021.3/0.1.0/repo/ | https://edt.1c.ru/downloads/releases/plugins/dt-project-checks/edt-2021.3/0.1.0/repo.zip |


Далее для установки нужно выполнить следующие действия:

- В среде разработки 1C:Enterprise Development Tools (EDT) выберите пункт меню `Help – Install New Software` (`Справка – Установить новое ПО`).
- В открывшемся окне мастера установки в строке `Work with` воспользуйтесь кнопкой `Add...` и укажите расположение репозитория.
- Если установка производится непосредственно из репозитория, опубликованного на серверах `фирмы 1С`, то скопируйте указанный адрес репозитория
- Если установка производится из локальной папки, то воспользуйтесь кнопкой `Local...` и далее по кнопке `Local` укажите папку, в которую распакован репозиторий.
- Отметьте компонент `1C:EDT Project checks` и нажмите кнопку `Next>`
- На следующем шаге система определит зависимости и сформирует окончательный список библиотек к установке, после этого нажмите кнопку `Next>`
- Прочитайте и примите условия лицензионного соглашения и нажмите кнопку `Finish`
- Дождитесь окончания установки и перезапустите среду `1C:Enterprise Development Tools`. Установка завершена.


## Участие в проекте

Добро пожаловать! [См. правила](CONTRIBUTING.md) в соответствующем разделе.

## Лицензия

[Лицензирование расширений размещенных в данном проекте осуществляется на условиях свободной (открытой) лицензии Eclipse Public License - v 2.0](LICENSE.md) (полный текст лицензии - https://www.eclipse.org/legal/epl-2.0/)
