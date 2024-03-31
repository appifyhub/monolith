# coding: utf-8

"""
    Appify Hub's Creator API

    The full specification of the service's API used by the project administrators.

    The version of the OpenAPI document: Latest
    Contact: contact@appifyhub.com
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


import unittest

from appifyhub.models.message_template_response import MessageTemplateResponse

class TestMessageTemplateResponse(unittest.TestCase):
    """MessageTemplateResponse unit test stubs"""

    def setUp(self):
        pass

    def tearDown(self):
        pass

    def make_instance(self, include_optional) -> MessageTemplateResponse:
        """Test MessageTemplateResponse
            include_option is a boolean, when False only required
            params are included, when True both required and
            optional params are included """
        # uncomment below to create an instance of `MessageTemplateResponse`
        """
        model = MessageTemplateResponse()
        if include_optional:
            return MessageTemplateResponse(
                id = 56,
                name = '',
                language_tag = '',
                title = '',
                content = '',
                is_html = True,
                created_at = '',
                updated_at = ''
            )
        else:
            return MessageTemplateResponse(
                id = 56,
                name = '',
                language_tag = '',
                title = '',
                content = '',
                is_html = True,
                created_at = '',
                updated_at = '',
        )
        """

    def testMessageTemplateResponse(self):
        """Test MessageTemplateResponse"""
        # inst_req_only = self.make_instance(include_optional=False)
        # inst_req_and_optional = self.make_instance(include_optional=True)

if __name__ == '__main__':
    unittest.main()
