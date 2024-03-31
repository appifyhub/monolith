# CreatorCredentialsRequest

A request with creator credentials

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**universal_id** | **str** | The universal ID of the object (creator ID and project ID separated by a dollar sign) | 
**signature** | **str** | The secret signature of the creator, usually a plain-text password | 
**origin** | **str** | The origin of the request | [optional] 

## Example

```python
from appifyhub.models.creator_credentials_request import CreatorCredentialsRequest

# TODO update the JSON string below
json = "{}"
# create an instance of CreatorCredentialsRequest from a JSON string
creator_credentials_request_instance = CreatorCredentialsRequest.from_json(json)
# print the JSON string representation of the object
print(CreatorCredentialsRequest.to_json())

# convert the object into a dict
creator_credentials_request_dict = creator_credentials_request_instance.to_dict()
# create an instance of CreatorCredentialsRequest from a dict
creator_credentials_request_form_dict = creator_credentials_request.from_dict(creator_credentials_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


