# UserCredentialsRequest

A request with user credentials

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**universal_id** | **str** | The universal ID of the object (user ID and project ID separated by a dollar sign) | 
**signature** | **str** | The secret signature of the user, usually a plain-text password | 
**origin** | **str** | The origin of the request | [optional] 

## Example

```python
from appifyhub.models.user_credentials_request import UserCredentialsRequest

# TODO update the JSON string below
json = "{}"
# create an instance of UserCredentialsRequest from a JSON string
user_credentials_request_instance = UserCredentialsRequest.from_json(json)
# print the JSON string representation of the object
print(UserCredentialsRequest.to_json())

# convert the object into a dict
user_credentials_request_dict = user_credentials_request_instance.to_dict()
# create an instance of UserCredentialsRequest from a dict
user_credentials_request_from_dict = UserCredentialsRequest.from_dict(user_credentials_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


