from rest_framework import serializers
from django.contrib.auth import authenticate
from django.contrib.auth.password_validation import validate_password
from .models import User, Vault, VaultMember


class UserRegistrationSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, validators=[validate_password])
    password_confirm = serializers.CharField(write_only=True)
    
    class Meta:
        model = User
        fields = ('email', 'username', 'name', 'password', 'password_confirm')
    
    def validate(self, attrs):
        if attrs['password'] != attrs['password_confirm']:
            raise serializers.ValidationError("Las contraseñas no coinciden.")
        return attrs
    
    def create(self, validated_data):
        validated_data.pop('password_confirm')
        user = User.objects.create_user(**validated_data)
        return user


class UserLoginSerializer(serializers.Serializer):
    email = serializers.EmailField()
    password = serializers.CharField()
    
    def validate(self, attrs):
        email = attrs.get('email')
        password = attrs.get('password')
        
        if email and password:
            user = authenticate(username=email, password=password)
            if not user:
                raise serializers.ValidationError('Credenciales inválidas.')
            if not user.is_active:
                raise serializers.ValidationError('La cuenta está desactivada.')
            attrs['user'] = user
            return attrs
        else:
            raise serializers.ValidationError('Debe incluir email y contraseña.')


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('id', 'email', 'username', 'name', 'avatar', 'phone', 
                 'birth_date', 'is_premium', 'created_at', 'updated_at')
        read_only_fields = ('id', 'created_at', 'updated_at')


class UserUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('name', 'avatar', 'phone', 'birth_date')
    
    def update(self, instance, validated_data):
        for attr, value in validated_data.items():
            setattr(instance, attr, value)
        instance.save()
        return instance


class VaultSerializer(serializers.ModelSerializer):
    owner_name = serializers.CharField(source='owner.name', read_only=True)
    member_count = serializers.SerializerMethodField()
    
    class Meta:
        model = Vault
        fields = ('id', 'name', 'description', 'owner', 'owner_name', 
                 'is_public', 'member_count', 'created_at', 'updated_at')
        read_only_fields = ('id', 'owner', 'created_at', 'updated_at')
    
    def get_member_count(self, obj):
        return obj.members.count()
    
    def create(self, validated_data):
        validated_data['owner'] = self.context['request'].user
        return super().create(validated_data)


class VaultMemberSerializer(serializers.ModelSerializer):
    user_name = serializers.CharField(source='user.name', read_only=True)
    user_email = serializers.CharField(source='user.email', read_only=True)
    
    class Meta:
        model = VaultMember
        fields = ('id', 'user', 'user_name', 'user_email', 'role', 'joined_at')
        read_only_fields = ('id', 'joined_at')


class VaultDetailSerializer(VaultSerializer):
    members = VaultMemberSerializer(many=True, read_only=True)
    
    class Meta(VaultSerializer.Meta):
        fields = VaultSerializer.Meta.fields + ('members',)
