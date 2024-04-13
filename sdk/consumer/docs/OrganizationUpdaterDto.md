# OrganizationUpdaterDto

A request to update the organization. Only the fields that are explicitly set will be updated. For expected data types, check the [OrganizationDto](#/components/schemas/OrganizationDto). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**street** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**postcode** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**city** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**country_code** | [**SettableRequest**](SettableRequest.md) |  | [optional] 

## Example

```python
from appifyhub.models.organization_updater_dto import OrganizationUpdaterDto

# TODO update the JSON string below
json = "{}"
# create an instance of OrganizationUpdaterDto from a JSON string
organization_updater_dto_instance = OrganizationUpdaterDto.from_json(json)
# print the JSON string representation of the object
print(OrganizationUpdaterDto.to_json())

# convert the object into a dict
organization_updater_dto_dict = organization_updater_dto_instance.to_dict()
# create an instance of OrganizationUpdaterDto from a dict
organization_updater_dto_from_dict = OrganizationUpdaterDto.from_dict(organization_updater_dto_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


