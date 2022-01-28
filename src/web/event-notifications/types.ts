export type TeamsNotificationSummaryType = {
    type: String,
    notification: NotificationType,
    
}

export type NotificationType = {
    config: ConfigType,
}

export interface ConfigType {
    defaultValue?: any,
    graylog_url?: String,
    icon_url?: String,
    backlog_size?: number,
    custom_message: String,
    webhook_url?: String,
    color?: String,
}

export type ValidationType = {
    failed?: boolean,
    error?: ErrorType
    
}

export interface ErrorType {
    webhook_url: string[],
    color: string[],
    icon_url: string,
    backlog_size: number,
    custom_message: string,
}