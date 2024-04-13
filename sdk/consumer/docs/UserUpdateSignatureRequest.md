# UserUpdateSignatureRequest

A request to update a user's signature

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**raw_signature_old** | **str** | The secret signature of the user, usually a plain-text password | 
**raw_signature_new** | **str** | The secret signature of the user, usually a plain-text password | 

## Example

```python
from appifyhub.models.user_update_signature_request import UserUpdateSignatureRequest

# TODO update the JSON string below
json = "{}"
# create an instance of UserUpdateSignatureRequest from a JSON string
user_update_signature_request_instance = UserUpdateSignatureRequest.from_json(json)
# print the JSON string representation of the object
print(UserUpdateSignatureRequest.to_json())

# convert the object into a dict
user_update_signature_request_dict = user_update_signature_request_instance.to_dict()
# create an instance of UserUpdateSignatureRequest from a dict
user_update_signature_request_from_dict = UserUpdateSignatureRequest.from_dict(user_update_signature_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


