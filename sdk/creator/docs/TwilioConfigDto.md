# TwilioConfigDto

Twilio account configuration, primarily used for SMS

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**account_sid** | **str** | An active account SID | 
**auth_token** | **str** | A valid auth token | 
**messaging_service_id** | **str** | A valid Messaging Service ID | 
**max_price_per_message** | **int** | The maximum price per message (in USD) | 
**max_retry_attempts** | **int** | The maximum retry attempts on message sending | 
**default_sender_name** | **str** | The name of the default sender. Set to blank to clear | 
**default_sender_number** | **str** | The phone number of the default sender | 

## Example

```python
from appifyhub.models.twilio_config_dto import TwilioConfigDto

# TODO update the JSON string below
json = "{}"
# create an instance of TwilioConfigDto from a JSON string
twilio_config_dto_instance = TwilioConfigDto.from_json(json)
# print the JSON string representation of the object
print(TwilioConfigDto.to_json())

# convert the object into a dict
twilio_config_dto_dict = twilio_config_dto_instance.to_dict()
# create an instance of TwilioConfigDto from a dict
twilio_config_dto_form_dict = twilio_config_dto.from_dict(twilio_config_dto_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


