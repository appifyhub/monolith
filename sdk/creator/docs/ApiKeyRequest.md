# ApiKeyRequest

A request for an API key

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**origin** | **str** | The origin of the request | [optional] 

## Example

```python
from appifyhub.models.api_key_request import ApiKeyRequest

# TODO update the JSON string below
json = "{}"
# create an instance of ApiKeyRequest from a JSON string
api_key_request_instance = ApiKeyRequest.from_json(json)
# print the JSON string representation of the object
print(ApiKeyRequest.to_json())

# convert the object into a dict
api_key_request_dict = api_key_request_instance.to_dict()
# create an instance of ApiKeyRequest from a dict
api_key_request_from_dict = ApiKeyRequest.from_dict(api_key_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


