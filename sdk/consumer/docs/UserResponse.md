# UserResponse

The response containing the user details

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**user_id** | **str** | A unique user identifier within the project. Depending on the project configuration, it can be differently formatted or even auto-generated.  | 
**project_id** | **int** | A unique project identifier | 
**universal_id** | **str** | The universal ID of the object (user ID and project ID separated by a dollar sign) | 
**name** | **str** | The name of the user | [optional] 
**type** | [**UserType**](UserType.md) |  | 
**authority** | [**Authority**](Authority.md) |  | 
**allows_spam** | **bool** | Whether the user allows spam or not | 
**contact** | **str** | The contact channel for the user (depends on the contact type) | [optional] 
**contact_type** | [**UserContactType**](UserContactType.md) |  | [optional] 
**birthday** | **date** | The birthday of the user | [optional] 
**company** | [**OrganizationDto**](OrganizationDto.md) |  | [optional] 
**language_tag** | **str** | The default language of the user (locale represented as in IETF BCP 47) | [optional] 
**created_at** | **str** | The time the object was created (based on ISO 8601) | 
**updated_at** | **str** | The time the object was last updated (based on ISO 8601) | 

## Example

```python
from appifyhub.models.user_response import UserResponse

# TODO update the JSON string below
json = "{}"
# create an instance of UserResponse from a JSON string
user_response_instance = UserResponse.from_json(json)
# print the JSON string representation of the object
print(UserResponse.to_json())

# convert the object into a dict
user_response_dict = user_response_instance.to_dict()
# create an instance of UserResponse from a dict
user_response_from_dict = UserResponse.from_dict(user_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


