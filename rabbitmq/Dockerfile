FROM rabbitmq:3.13.0-management

ADD https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v3.13.0/rabbitmq_delayed_message_exchange-3.13.0.ez /opt/rabbitmq/plugins/

# Чиним права (ЭТО КЛЮЧЕВО)
RUN chown rabbitmq:rabbitmq /opt/rabbitmq/plugins/rabbitmq_delayed_message_exchange-3.13.0.ez \
    && chmod 644 /opt/rabbitmq/plugins/rabbitmq_delayed_message_exchange-3.13.0.ez \
    && rabbitmq-plugins enable --offline rabbitmq_delayed_message_exchange
