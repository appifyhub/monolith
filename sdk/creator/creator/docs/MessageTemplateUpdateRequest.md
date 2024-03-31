# MessageTemplateUpdateRequest

A request to update a message template. Only the fields that are explicitly set will be updated. For expected data types, check the [MessageTemplateCreateRequest](#/components/schemas/MessageTemplateCreateRequest). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**language_tag** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**title** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**content** | [**SettableRequest**](SettableRequest.md) |  | [optional] 
**is_html** | [**SettableRequest**](SettableRequest.md) |  | [optional] 

## Example

```python
from appifyhub.models.message_template_update_request import MessageTemplateUpdateRequest

# TODO update the JSON string below
json = "{}"
# create an instance of MessageTemplateUpdateRequest from a JSON string
message_template_update_request_instance = MessageTemplateUpdateRequest.from_json(json)
# print the JSON string representation of the object
print(MessageTemplateUpdateRequest.to_json())

# convert the object into a dict
message_template_update_request_dict = message_template_update_request_instance.to_dict()
# create an instance of MessageTemplateUpdateRequest from a dict
message_template_update_request_form_dict = message_template_update_request.from_dict(message_template_update_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


