#!/bin/bash

# Local Development Runner Script
# This script sets up environment variables and runs the Spring Boot application

set -e  # Exit on error

echo "🚀 Starting DriveFlow Backend (Local Development)"
echo ""

# Check if JWT_SECRET is already set
if [ -z "$JWT_SECRET" ]; then
    echo "⚠️  JWT_SECRET not set. Generating a new one..."
    export JWT_SECRET=$(openssl rand -base64 32)
    echo "✅ Generated JWT_SECRET: $JWT_SECRET"
    echo ""
    echo "💡 Tip: Save this JWT_SECRET for future runs, or set it in your IDE run configuration"
    echo ""
else
    echo "✅ Using existing JWT_SECRET"
fi

# Set default values if not already set
export MAIL_HOST=${MAIL_HOST:-smtp.gmail.com}
export MAIL_PORT=${MAIL_PORT:-587}
export MAIL_SMTP_AUTH=${MAIL_SMTP_AUTH:-true}
export MAIL_STARTTLS_ENABLE=${MAIL_STARTTLS_ENABLE:-true}

# Check for required email variables
if [ -z "$MAIL_USERNAME" ] || [ -z "$MAIL_PASSWORD" ]; then
    echo "⚠️  Warning: MAIL_USERNAME or MAIL_PASSWORD not set!"
    echo "   Email functionality may not work."
    echo "   Set these in your environment or IDE run configuration."
    echo ""
fi

# Database defaults (for local PostgreSQL)
export DATABASE_URL=${DATABASE_URL:-jdbc:postgresql://localhost:5432/driveflow}
export DB_USERNAME=${DB_USERNAME:-postgres}
export DB_PASSWORD=${DB_PASSWORD:-password}

echo "📋 Environment Variables:"
echo "   JWT_SECRET: [SET]"
echo "   MAIL_HOST: $MAIL_HOST"
echo "   MAIL_PORT: $MAIL_PORT"
echo "   MAIL_USERNAME: ${MAIL_USERNAME:-[NOT SET]}"
echo "   MAIL_PASSWORD: ${MAIL_PASSWORD:+[SET]}${MAIL_PASSWORD:-[NOT SET]}"
echo ""

# Run the application
echo "🏃 Starting Spring Boot application..."
echo ""
mvn spring-boot:run

