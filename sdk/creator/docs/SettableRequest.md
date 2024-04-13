# SettableRequest

A value wrapper for a value

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**value** | **object** | The value to set, or no value to unset | 

## Example

```python
from appifyhub.models.settable_request import SettableRequest

# TODO update the JSON string below
json = "{}"
# create an instance of SettableRequest from a JSON string
settable_request_instance = SettableRequest.from_json(json)
# print the JSON string representation of the object
print(SettableRequest.to_json())

# convert the object into a dict
settable_request_dict = settable_request_instance.to_dict()
# create an instance of SettableRequest from a dict
settable_request_from_dict = SettableRequest.from_dict(settable_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


