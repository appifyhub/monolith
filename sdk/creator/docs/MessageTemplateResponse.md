# MessageTemplateResponse

A response containing a message template

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **int** | The ID of the message template to use | 
**name** | **str** | The (unique) name of the message template | 
**language_tag** | **str** | The default language of the message template (locale represented as in IETF BCP 47) | 
**title** | **str** | The title of the message template | 
**content** | **str** | The template text content, including variables | 
**is_html** | **bool** | Whether the content is HTML or not | 
**created_at** | **str** | The time the object was created (based on ISO 8601) | 
**updated_at** | **str** | The time the object was last updated (based on ISO 8601) | 

## Example

```python
from appifyhub.models.message_template_response import MessageTemplateResponse

# TODO update the JSON string below
json = "{}"
# create an instance of MessageTemplateResponse from a JSON string
message_template_response_instance = MessageTemplateResponse.from_json(json)
# print the JSON string representation of the object
print(MessageTemplateResponse.to_json())

# convert the object into a dict
message_template_response_dict = message_template_response_instance.to_dict()
# create an instance of MessageTemplateResponse from a dict
message_template_response_form_dict = message_template_response.from_dict(message_template_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


