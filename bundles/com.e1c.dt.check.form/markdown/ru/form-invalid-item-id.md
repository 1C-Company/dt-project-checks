# Правильность идентификаторов элементов формы

Проверяет, что элементы формы имеют верные идентификаторы.

Идентификатор элемента формы верен, если он имеет уникальное значение в рамках своей формы
(другие элементы данной формы не должны иметь такой же идентификатор).
Дополнительно, значение `0` не является верным для идентификатора.
При этом, отрицательные значения допустимы и автоматически назначаются в некоторых случаях.
Например, командной панели может назначаться идентификатор `-1`.

Данная проверка затрагивает только элементы формы.
Реквизиты и прочее содержимое данная проверка не анализирует.

## Неправильно

Следующий пример неверен, так как ни группа, ни подсказка не имеют дочернего элемента `<id>`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<form:Form xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:core="http://g5.1c.ru/v8/dt/mcore" xmlns:form="http://g5.1c.ru/v8/dt/form">
  <items xsi:type="form:FormGroup">
    <name>ListSettingsComposerUserSettings</name>
    <visible>true</visible>
    <enabled>true</enabled>
    <userVisible>
      <common>true</common>
    </userVisible>
    <title>
      <key>en</key>
      <value>User settings group</value>
    </title>
    <verticalStretch>false</verticalStretch>
    <extendedTooltip>
      <name>ListSettingsComposerUserSettingsExtendedTooltip</name>
      <visible>true</visible>
      <enabled>true</enabled>
      <userVisible>
        <common>true</common>
      </userVisible>
      <type>Label</type>
      <autoMaxWidth>true</autoMaxWidth>
      <autoMaxHeight>true</autoMaxHeight>
      <extInfo xsi:type="form:LabelDecorationExtInfo">
        <horizontalAlign>Left</horizontalAlign>
      </extInfo>
    </extendedTooltip>
    <type>UsualGroup</type>
```

Следующий пример неверен, так как элемент `<id>` поля `Code` имеет значение `0`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<form:Form xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:form="http://g5.1c.ru/v8/dt/form">
  <items xsi:type="form:FormField">
    <name>Code</name>
    <id>0</id>
    <visible>true</visible>
    <enabled>true</enabled>
    <userVisible>
      <common>true</common>
    </userVisible>
    <dataPath xsi:type="form:DataPath">
      <segments>Object.Code</segments>
    </dataPath>
```

Следующий пример иллюстрирует возможный результат слияния изменений, 
когда двое пользователей одновременно редактировали одну и туже форму,
и каждый добавил свое поле. Обратите внимание, что оба поля имеют одинаковые значения
элемента `<id>`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<form:Form xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:core="http://g5.1c.ru/v8/dt/mcore" xmlns:form="http://g5.1c.ru/v8/dt/form">
    <items xsi:type="form:FormField">
      <name>Description</name>
      <id>19</id>
      <visible>true</visible>
      <enabled>true</enabled>
      <userVisible>
        <common>true</common>
      </userVisible>
      <dataPath xsi:type="form:DataPath">
        <segments>List.Description</segments>
      </dataPath>
      <type>LabelField</type>
      <editMode>Enter</editMode>
      <showInHeader>true</showInHeader>
      <headerHorizontalAlign>Left</headerHorizontalAlign>
      <showInFooter>true</showInFooter>
    </items>
    <items xsi:type="form:FormField">
      <name>Code</name>
      <id>19</id>
      <visible>true</visible>
      <enabled>true</enabled>
      <userVisible>
        <common>true</common>
      </userVisible>
      <dataPath xsi:type="form:DataPath">
        <segments>List.Code</segments>
      </dataPath>
      <type>LabelField</type>
      <editMode>Enter</editMode>
      <showInHeader>true</showInHeader>
      <headerHorizontalAlign>Left</headerHorizontalAlign>
      <showInFooter>true</showInFooter>
    </items>
``` 

## Правильно

В следующем примере проиллюстрирована правильная форма, в которой каждому элементу назначено
уникальное не нулевое значение идентификатора (элемент `<id>`):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<form:Form xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:form="http://g5.1c.ru/v8/dt/form">
  <items xsi:type="form:FormField">
    <name>Code</name>
    <id>1</id>
    <visible>true</visible>
    <enabled>true</enabled>
    <userVisible>
      <common>true</common>
    </userVisible>
    <dataPath xsi:type="form:DataPath">
      <segments>Object.Code</segments>
    </dataPath>
    <contextMenu>
      <name>CodeContextMenu</name>
      <id>2</id>
      <visible>true</visible>
      <enabled>true</enabled>
      <userVisible>
        <common>true</common>
      </userVisible>
      <autoFill>true</autoFill>
    </contextMenu>
    <type>InputField</type>
    <editMode>EnterOnInput</editMode>
    <showInHeader>true</showInHeader>
    <headerHorizontalAlign>Left</headerHorizontalAlign>
    <showInFooter>true</showInFooter>
    <extInfo xsi:type="form:InputFieldExtInfo">
      <autoMaxWidth>true</autoMaxWidth>
      <autoMaxHeight>true</autoMaxHeight>
      <wrap>true</wrap>
      <chooseType>true</chooseType>
      <typeDomainEnabled>true</typeDomainEnabled>
      <textEdit>true</textEdit>
    </extInfo>
  </items>
  <autoCommandBar>
    <name>FormCommandBar</name>
    <id>-1</id>
    <visible>true</visible>
    <enabled>true</enabled>
    <userVisible>
      <common>true</common>
    </userVisible>
    <horizontalAlign>Left</horizontalAlign>
    <autoFill>true</autoFill>
  </autoCommandBar>
  <windowOpeningMode>LockOwnerWindow</windowOpeningMode>
  <autoTitle>true</autoTitle>
  <autoUrl>true</autoUrl>
  <group>Vertical</group>
  <autoFillCheck>true</autoFillCheck>
  <allowFormCustomize>true</allowFormCustomize>
  <enabled>true</enabled>
  <showTitle>true</showTitle>
  <showCloseButton>true</showCloseButton>
  <attributes>
    <name>Object</name>
    <id>1</id>
    <valueType>
      <types>CatalogObject.Catalog</types>
    </valueType>
    <view>
      <common>true</common>
    </view>
    <edit>
      <common>true</common>
    </edit>
    <main>true</main>
    <savedData>true</savedData>
  </attributes>
  <commandInterface>
    <navigationPanel/>
    <commandBar/>
  </commandInterface>
  <extInfo xsi:type="form:CatalogFormExtInfo"/>
</form:Form>
```

## См.

- [Общие требования к конфигурации](https://its.1c.ru/db/v8std#content:467:hdoc:2.2)
