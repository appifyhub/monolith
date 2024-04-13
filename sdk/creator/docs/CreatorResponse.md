# CreatorResponse

A user's details

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**user_id** | **str** | The creator&#39;s unique ID | 
**project_id** | **int** | A unique identifier for this consumer project | 
**universal_id** | **str** | The universal ID of the object (creator ID and project ID separated by a dollar sign) | 
**name** | **str** | The name of the creator | [optional] 
**type** | **str** | The type of the creator | 
**authority** | **str** | The authority of the creator | 
**allows_spam** | **bool** | Whether the creator allows spam or not | 
**contact** | **str** | The contact of the creator | [optional] 
**contact_type** | **str** | The type of contact for this creator | 
**birthday** | **date** | The birthday of the creator | [optional] 
**company** | [**OrganizationDto**](OrganizationDto.md) |  | [optional] 
**language_tag** | **str** | The default language of the creator (locale represented as in IETF BCP 47) | [optional] 
**created_at** | **str** | The time the object was created (based on ISO 8601) | 
**updated_at** | **str** | The time the object was last updated (based on ISO 8601) | 

## Example

```python
from appifyhub.models.creator_response import CreatorResponse

# TODO update the JSON string below
json = "{}"
# create an instance of CreatorResponse from a JSON string
creator_response_instance = CreatorResponse.from_json(json)
# print the JSON string representation of the object
print(CreatorResponse.to_json())

# convert the object into a dict
creator_response_dict = creator_response_instance.to_dict()
# create an instance of CreatorResponse from a dict
creator_response_from_dict = CreatorResponse.from_dict(creator_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


