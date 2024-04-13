# UserUpdateAuthorityRequest

A request to update a user's authority level

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**authority** | [**Authority**](Authority.md) |  | 

## Example

```python
from appifyhub.models.user_update_authority_request import UserUpdateAuthorityRequest

# TODO update the JSON string below
json = "{}"
# create an instance of UserUpdateAuthorityRequest from a JSON string
user_update_authority_request_instance = UserUpdateAuthorityRequest.from_json(json)
# print the JSON string representation of the object
print(UserUpdateAuthorityRequest.to_json())

# convert the object into a dict
user_update_authority_request_dict = user_update_authority_request_instance.to_dict()
# create an instance of UserUpdateAuthorityRequest from a dict
user_update_authority_request_from_dict = UserUpdateAuthorityRequest.from_dict(user_update_authority_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


