# OrganizationUpdaterSettable

A value wrapper for the organization property

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**value** | [**OrganizationUpdaterDto**](OrganizationUpdaterDto.md) |  | 

## Example

```python
from appifyhub.models.organization_updater_settable import OrganizationUpdaterSettable

# TODO update the JSON string below
json = "{}"
# create an instance of OrganizationUpdaterSettable from a JSON string
organization_updater_settable_instance = OrganizationUpdaterSettable.from_json(json)
# print the JSON string representation of the object
print(OrganizationUpdaterSettable.to_json())

# convert the object into a dict
organization_updater_settable_dict = organization_updater_settable_instance.to_dict()
# create an instance of OrganizationUpdaterSettable from a dict
organization_updater_settable_from_dict = OrganizationUpdaterSettable.from_dict(organization_updater_settable_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


