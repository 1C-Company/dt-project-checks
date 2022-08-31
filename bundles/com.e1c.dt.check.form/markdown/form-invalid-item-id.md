# Form item identifier correctness

Checks that form items have valid identifiers.

Form item identifier is valid if it has a unique value across all other items on this form.
Additionally, an identifier is considered to be invalid if its value is `0`.
Negative values are not considered to be invalid. That is because such values are perfectly valid
at least for some of the cases. For example, auto command bar might have `-1` as in identifier.

Only form items are checked. Form attributes and other child content is ignored.

## Noncompliant Code Example

The following example is incorrect because neither form group, nor extended tool tip have
child element `<id>`:

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

The following example is incorrect because `<id>` element of `Code` field has a value of `0`:

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

The following example illustrates result of a merge after two users have edited the same form and each added a field.
Note that there are two fields with the same value of `<id>` element:

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

## Compliant Solution

The following example illustrates correct form when each item has child `<id>` element
with unique non-zero value:

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

## See

- [General configuration requirements](https://support.1ci.com/hc/en-us/articles/360011107839-General-configuration-requirements)
