# MessageResponse

A response containing the message details

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template** | [**MessageTemplateResponse**](MessageTemplateResponse.md) |  | 
**materialized** | **str** | The materialized message with all variables replaced | 

## Example

```python
from appifyhub.models.message_response import MessageResponse

# TODO update the JSON string below
json = "{}"
# create an instance of MessageResponse from a JSON string
message_response_instance = MessageResponse.from_json(json)
# print the JSON string representation of the object
print(MessageResponse.to_json())

# convert the object into a dict
message_response_dict = message_response_instance.to_dict()
# create an instance of MessageResponse from a dict
message_response_form_dict = message_response.from_dict(message_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


