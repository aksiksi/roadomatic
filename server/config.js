config = {};

// Database configuration
config.db_url = 'mongodb://localhost:27017/roadomatic';
config.roads = 'roads';
config.segments = 'segments';

// UDP server configuration
config.server_host = '0.0.0.0';
config.server_port = 5151;
config.server_encrypt = true;

// Frontend app configuration
config.frontend_host = '0.0.0.0';
config.frontend_port = 5152;
config.static_dir = 'static';
config.views_dir = 'views';

module.exports = config;
