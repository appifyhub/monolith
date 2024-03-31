# OrganizationDto

The organization's details

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | The name of the organization | [optional] 
**street** | **str** | The street of the organization | [optional] 
**postcode** | **str** | The postcode of the organization | [optional] 
**city** | **str** | The city of the organization | [optional] 
**country_code** | **str** | The 2-letter ISO country code of the organization | [optional] 

## Example

```python
from appifyhub.models.organization_dto import OrganizationDto

# TODO update the JSON string below
json = "{}"
# create an instance of OrganizationDto from a JSON string
organization_dto_instance = OrganizationDto.from_json(json)
# print the JSON string representation of the object
print(OrganizationDto.to_json())

# convert the object into a dict
organization_dto_dict = organization_dto_instance.to_dict()
# create an instance of OrganizationDto from a dict
organization_dto_form_dict = organization_dto.from_dict(organization_dto_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


