
//
// Disclaimer:
// ----------
//
// This code will work only if you selected window, graphics and audio.
//
// In order to load the resources like cute_image.png, you have to set up
// your target scheme:
//
// - Select "Edit Scheme…" in the "Product" menu;
// - Check the box "use custom working directory";
// - Fill the text field with the folder path containing your resources;
//        (e.g. your project folder)
// - Click OK.
//
#include <stdlib.h>
#include <math.h>
#include <iostream>
#include <utility>
#include <SFML/Window.hpp>
#include <SFML/Graphics.hpp>
#include <array>
#include <sstream>

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                        //Variables
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

struct colorStruct{
    float r;
    float g;
    float b;
};
// WINDOW DIMENSIONS
const int pixelHorizontal=2000;
const int pixelVertical=1400;

// PHEROMON PARAMETERS
float pheromonStrength = 30.0;
float decayRate=0.2;
float pheromonPerception = 600;
float defaultPheromonColourR=240;
float defaultPheromonColourG=190;
float defaultPheromonColourB=70;
std::array<std::array<float,pixelVertical+1>,pixelHorizontal+1> pheromonArray;
sf::VertexArray pheromonVertexArray(sf::Points, ((pixelVertical+1)*(pixelHorizontal+1)));
float progressPheromonFloatVariable=0;

// BOID PARAMETERS
float maxGForce = 0.5;//0.5; //1; // 0.01;
float maxSpeed = 4;  // 0.3;
float minSpeed = 2;
float perception = 100; //100
//float minDist2Obst = 50;

// RESULTS
float time2TargetAcquired = 0.;

// VONORI PARAMETERS
int radiusVonori = 0;
int distVonori = 0;
 bool firstT=true;
bool firstTimeVonori = true;
bool restartSim=false;
std::vector<int>vonoriPosX;
std::vector<int>vonoriPosY;
sf::VertexArray vonoriVertexArray(sf::Points, ((pixelVertical)*(pixelHorizontal)));

std::vector<std::map<int,int>> boidMapVector;

// SIM VARIABLES  - DO NOT CHANGE -
bool simulationActive = true;
bool visualizationActive = false;
bool pheromonActive = false;
bool boundariesActive = false;
bool jamSamSitesActive = false;
bool targetAcquired = false;
bool AttackActive = false;
//bool first = true;
bool vonoriActive = false;
bool seperationActive = false;
bool protectAircraftActive = false;

// SIM Entities
sf::Font font;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                        //General Helper Functions
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

std::string limitFloat2String(float f, int limit){
    std::stringstream s;
    s.precision(limit);
    s << f;
    
    return s.str();
};

void setStringCharakteristics(sf::Text& text,float x, float y, std::string word){
    text.setFont(font);
    text.setString(word);
    text.setCharacterSize(28);
    text.setFillColor(sf::Color::White);
    text.setStyle(sf::Text::Bold);
    text.setPosition(x, y);
};


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                        //PVECTOR CLASS
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class PVector{
private:
    float x;
    float y;
public:
    PVector(float x1,float y1);
    PVector();
    void add(PVector v);
    void sub(PVector v);
    void div(PVector v);
    float AoD(PVector v1, PVector v2);
    void multValue(float f);
    void random2D();
    float heading();
    void addContent(float x1,float y1);
    void setContent(float x1,float y1);
    float returnX();
    float returnY();
    void writeX(float x1);
    void writeY(float y1);
    float magnitude();
    std::pair<float, float> returnPair();
    PVector operator+(PVector& other);
    void operator+=(PVector& other);
    void operator/(int div);
    void setMag(float limit);
    void rotate(float degree);
    
    
};
PVector::PVector(){
    
}

PVector::PVector(float x1,float y1){
    x=x1;
    y=y1;
}

PVector PVector::operator+(PVector& other)
{
    auto new_x = x + other.x;
    auto new_y = y + other.y;
    return PVector(new_x, new_y);
}

void PVector::operator+=(PVector& other)
{
    x += other.x;
    y += other.y;
}



std::pair<float,float> PVector::returnPair(){
    return {x,y};
}
void PVector::add(PVector v){
    x = x+v.x;
    y = y+v.y;
}

void PVector::sub(PVector v){
    x = x - v.x;
    y = y - v.y;
}

void PVector::div(PVector v){
    x = x/v.x;
    y = y/v.y;
}

void PVector::multValue(float f){
    x = f*x;
    y = f*y;
}

void PVector::random2D(){
    x = 8. * ((float)rand()/RAND_MAX) - 4.;
    y = 8. * ((float)rand()/RAND_MAX) - 4.;
}

float PVector::heading(){
    float result;
    
    PVector yAxis(0,-1);
    
    if(x < 0){
        result = -1.*(acos((x*yAxis.x+y*yAxis.y)/(magnitude()*yAxis.magnitude())))* (180/M_PI);
    } else if (x > 0){
        result = acos((x*yAxis.x+y*yAxis.y)/(magnitude()*yAxis.magnitude()))* (180/M_PI);
    }
    
    return result;
}

void PVector::addContent(float x1, float y1){
    x = x + x1;
    y = y + y1;
}

void PVector::setContent(float x1, float y1){
    x = x1;
    y = y1;
}

float PVector::returnX(){
    return x;
}

float PVector::returnY(){
    return y;
}

void PVector::writeX(float x1){
    x = x1;
}

void PVector::writeY(float y1){
    y = y1;
}
            
float PVector::magnitude(){
    return sqrt(pow(x,2)+pow(y,2));
            }

void PVector::operator/(int div){
    x = x/div;
    y = y/div;
}

void PVector::setMag(float limit){
    float d = sqrt(pow(x,2)+pow(y,2))/limit;
    x = x/d;
    y = y/d;
}

float PVector::AoD(PVector v1, PVector v2){
    float res = acos(((v1.x*v2.x+v1.y*v2.y)/(sqrt(pow(v1.x,2)+pow(v1.y,2))*sqrt(pow(v2.x,2)+pow(v2.y,2)))));
    
    return res;
}

void PVector::rotate(float degree){
    float rad = M_PI/180 * degree;
    float x1 = x;
    float y1 = y;
    x = cos(rad)*x1 - sin(rad)*y1;
    y = sin(rad)*x1 + cos(rad)*y1;
}

/////////////////////////////////////////////////////////////////////////////////////////////////
                                //OBSTACLE CLASS
/////////////////////////////////////////////////////////////////////////////////////////////////
class Obstacle{
private:
    sf::RectangleShape ObstacleShape;
    PVector position;
public:
    Obstacle(float x,float y);
    sf::RectangleShape returnObstacle();
    float returnPosX();
    float returnPosY();
    
};

Obstacle::Obstacle(float x,float y){
    ObstacleShape.setSize(sf::Vector2f(20.0f,20.0f));
    ObstacleShape.setFillColor(sf::Color(150,0,0));
    position.setContent(x,y);
    ObstacleShape.setPosition(x,y);
}

sf::RectangleShape Obstacle::returnObstacle(){
    
    return ObstacleShape;
}

float Obstacle::returnPosX(){
    return position.returnX();
}

float Obstacle::returnPosY(){
    return position.returnY();
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
                        //TARGET CLASS
///////////////////////////////////////////////////////////////////////////////////////////////////////
class Target{
private:
    sf::CircleShape TargetShape;
    PVector targetPosition;
    bool targetFirstlyAcquired = true;
public:
    Target(float x, float y);
    sf::CircleShape returnTarget();
    PVector returnTargetPos();
    void targetDetected();
};

Target::Target(float x, float y){
    TargetShape.setRadius(10.);
    TargetShape.setFillColor(sf::Color::Blue);
    targetPosition.setContent(x, y);
    TargetShape.setPosition(x, y);
}

sf::CircleShape Target::returnTarget(){
    return TargetShape;
}

PVector Target::returnTargetPos(){
    return targetPosition;
}

void Target::targetDetected(){
    if(targetFirstlyAcquired){
        targetAcquired = true;
    }
    targetFirstlyAcquired = false;
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
                        // ACF CLASS BLUE
/////////////////////////////////////////////////////////////////////////////////////////////////////////
class AircraftFriend{
private:
    PVector position;
    PVector velocity;
    PVector acceleration;
    float HeadingAcf;
    sf::CircleShape AcfShape;
public:
    AircraftFriend(float x, float y);
    sf::CircleShape returnAcf();
    void updateAcfs();
    PVector returnPosAcf();
    PVector stayInBoundaries();
    void calculateNewPosition();
};

AircraftFriend::AircraftFriend(float x,float y){
    position.setContent(x, y);
    velocity.random2D();
    acceleration.setContent(0,0);
    
    
    AcfShape.setRadius(20.0f);
    AcfShape.setPointCount(3);
    AcfShape.setFillColor(sf::Color::Blue);
    AcfShape.setOutlineThickness(1.0f);
    AcfShape.setOutlineColor(sf::Color(250,250,250));
    AcfShape.setPosition(x,y);
}

sf::CircleShape AircraftFriend::returnAcf(){
    return AcfShape;
}

void AircraftFriend::updateAcfs(){
    velocity.add(acceleration);
    velocity.setMag(maxSpeed);
    position.add(velocity);
    
    // In case boundaries are not active the acf is appearing on the opposite side of the screen
    if(position.returnX() > pixelHorizontal+15){
        position.writeX(0);
    }
    if(position.returnX() < -20){
        position.writeX(pixelHorizontal);
    }
    if(position.returnY() > pixelVertical+15){
        position.writeY(0);
    }
    if(position.returnY() < -20){
        position.writeY(pixelVertical);
    }
    auto [x,y] = position.returnPair();
    AcfShape.setPosition(x,y);
    HeadingAcf = velocity.heading();
    AcfShape.setRotation(HeadingAcf);
}
PVector AircraftFriend::returnPosAcf(){
    return position;
}

PVector AircraftFriend::stayInBoundaries(){
    PVector Desired(0,0);
    
    if(position.returnX() < 100){
        float xScaled = 100-position.returnX();
        if(HeadingAcf < -88 && HeadingAcf > -92){
            Desired.addContent(xScaled, 2);
        }else{
            Desired.addContent(xScaled,0);
        }
    }
    if(position.returnX() > pixelHorizontal-100){
        float xScaled = (pixelHorizontal-100)-position.returnX();
        if(HeadingAcf > 88 && HeadingAcf < 92){
            Desired.addContent(xScaled, 2);
        }else{
            Desired.addContent(xScaled,0);
        }
    }
    if(position.returnY() < 100){
        float yScaled = 100-position.returnY();
        if(HeadingAcf > -2 && HeadingAcf < 2){
            Desired.addContent(2, yScaled);
        }else{
            Desired.addContent(0, yScaled);
        }
    }
    if(position.returnY() > pixelVertical-100){
        float yScaled= (pixelVertical-100)-position.returnY();
        if((HeadingAcf < -178 && HeadingAcf >= -180) || (HeadingAcf > 178 && HeadingAcf <= 180)){
            Desired.addContent(2, yScaled);
        }else{
            Desired.addContent(0, yScaled);
        }
    }
    
    return Desired;
}

void AircraftFriend::calculateNewPosition(){
    
    if(boundariesActive){
        PVector BoundaryForce = stayInBoundaries();
        acceleration.addContent(BoundaryForce.returnX(), BoundaryForce.returnY());
    }
    
    if(acceleration.returnX() != 0 || acceleration.returnY() != 0){
        acceleration.setMag(maxGForce);
    }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
                        // ACF CLASS Red
/////////////////////////////////////////////////////////////////////////////////////////////////////////
class AircraftFoe{
private:
    PVector position;
    PVector velocity;
    PVector acceleration;
    float HeadingAcf;
    sf::CircleShape AcfShape;
public:
    AircraftFoe(float x, float y);
    sf::CircleShape returnAcf();
    void updateAcfs();
    PVector returnPosAcf();
    PVector stayInBoundaries();
    void calculateNewPosition();
};

AircraftFoe::AircraftFoe(float x,float y){
    position.setContent(x, y);
    velocity.random2D();
    acceleration.setContent(0,0);
    
    
    AcfShape.setRadius(20.0f);
    AcfShape.setPointCount(3);
    AcfShape.setFillColor(sf::Color::Red);
    AcfShape.setOutlineThickness(1.0f);
    AcfShape.setOutlineColor(sf::Color(250,250,250));
    AcfShape.setPosition(x,y);
}

sf::CircleShape AircraftFoe::returnAcf(){
    return AcfShape;
}

void AircraftFoe::updateAcfs(){
    velocity.add(acceleration);
    velocity.setMag(maxSpeed);
    position.add(velocity);
    
    // In case boundaries are not active the acf is appearing on the opposite side of the screen
    if(position.returnX() > pixelHorizontal+15){
        position.writeX(0);
    }
    if(position.returnX() < -20){
        position.writeX(pixelHorizontal);
    }
    if(position.returnY() > pixelVertical+15){
        position.writeY(0);
    }
    if(position.returnY() < -20){
        position.writeY(pixelVertical);
    }
    auto [x,y] = position.returnPair();
    AcfShape.setPosition(x,y);
    HeadingAcf = velocity.heading();
    AcfShape.setRotation(HeadingAcf);
}

PVector AircraftFoe::returnPosAcf(){
    return position;
}

PVector AircraftFoe::stayInBoundaries(){
    PVector Desired(0,0);
    
    if(position.returnX() < 100){
        float xScaled = 100-position.returnX();
        if(HeadingAcf < -88 && HeadingAcf > -92){
            Desired.addContent(xScaled, 2);
        }else{
            Desired.addContent(xScaled,0);
        }
    }
    if(position.returnX() > pixelHorizontal-100){
        float xScaled = (pixelHorizontal-100)-position.returnX();
        if(HeadingAcf > 88 && HeadingAcf < 92){
            Desired.addContent(xScaled, 2);
        }else{
            Desired.addContent(xScaled,0);
        }
    }
    if(position.returnY() < 100){
        float yScaled = 100-position.returnY();
        if(HeadingAcf > -2 && HeadingAcf < 2){
            Desired.addContent(2, yScaled);
        }else{
            Desired.addContent(0, yScaled);
        }
    }
    if(position.returnY() > pixelVertical-100){
        float yScaled= (pixelVertical-100)-position.returnY();
        if((HeadingAcf < -178 && HeadingAcf >= -180) || (HeadingAcf > 178 && HeadingAcf <= 180)){
            Desired.addContent(2, yScaled);
        }else{
            Desired.addContent(0, yScaled);
        }
    }
    
    return Desired;
}

void AircraftFoe::calculateNewPosition(){
    
    if(boundariesActive){
        PVector BoundaryForce = stayInBoundaries();
        acceleration.addContent(BoundaryForce.returnX(), BoundaryForce.returnY());
    }
    
    if(acceleration.returnX() != 0 || acceleration.returnY() != 0){
        acceleration.setMag(maxGForce);
    }
}
///////////////////////////////////////////////////////////////////////////////////////////////////////
                        //BOID CLASS
///////////////////////////////////////////////////////////////////////////////////////////////////////

class Boid{
private:
    PVector velocity;
    PVector acceleration;
    PVector position;
    float headingBoid;
    sf::Color vonoriCellCollorOfBoid;
    sf::CircleShape Boidshape;
public:
    Boid(float x,float y);
    sf::CircleShape returnBoid();
    void updateBoids();
    void flock(std::vector<Boid> *BVec, std::vector<Obstacle> *OVec, std::vector<Target> *TVec, std::vector<AircraftFriend> *BAcfVec, std::vector<AircraftFoe> *FAcfVec, std::vector<std::vector<PVector>> *BoidAssigned2Target2D);
    PVector align(const std::vector<Boid>& BVec);
    PVector cohesion(const std::vector<Boid>& BVec);
    PVector seperation(const std::vector<Boid>& BVec);
    PVector evasion(const std::vector<Obstacle>& OVec);
    PVector SEAD(std::vector<Target> TVec,const std::vector<Boid>& BVec, std::vector<std::vector<PVector>> *BoidAssigned2Target2D);
    PVector Attack(const std::vector<Boid>& BVec,std::vector<Target>& TVec,  std::vector<std::vector<PVector>> *BoidAssigned2Target2D);
    PVector protectAircraft(const std::vector<AircraftFriend>& BAcfVec, const std::vector<AircraftFoe>& FAcfVec);
    PVector returnPosBoid();
    PVector returnVelocityBoid();
    PVector returnDesiredPheromonDirection();
    PVector pheromonDirectionByGradient();
    PVector searchVonoriCells();
    PVector stayInBoundaries();
    PVector rotate(PVector Vec, float degree);
    void assignBoids2Targets(std::vector<Target> TVec,const std::vector<Boid>& BVec, std::vector<std::vector<PVector>> *BoidAssigned2Target2D);
    float returnPerception();
    void setVonoriCellCollorOfBoid(int r, int g, int b);
    sf::Color getVonoriCellCollorOfBoid();
    std::pair<float,float> limitForce(PVector force);
    
};

Boid::Boid(float x,float y){
    //std::cout << "Boid was created" << std::endl;
    position.setContent(x, y);
    velocity.random2D();
    acceleration.setContent(0,0);
    
    
    Boidshape.setRadius(10.0f);
    Boidshape.setPointCount(3);
    Boidshape.setFillColor(sf::Color(100,250,50));
    Boidshape.setOutlineThickness(1.0f);
    Boidshape.setOutlineColor(sf::Color(250,250,250));
    Boidshape.setPosition(x,y);
}

sf::CircleShape Boid::returnBoid(){
    
    return Boidshape;
}

void Boid::updateBoids(){
    velocity.add(acceleration);
    if(velocity.magnitude() > maxSpeed){
        velocity.setMag(maxSpeed);
    }
    if(velocity.magnitude() < minSpeed){
        velocity.setMag(minSpeed);
    }
    position.add(velocity);
    
    if(!boundariesActive){
        if(position.returnX() > pixelHorizontal+15){
            position.writeX(0);
        }
        if(position.returnX() < -20){
            position.writeX(pixelHorizontal);
        }
        if(position.returnY() > pixelVertical+15){
            position.writeY(0);
        }
        if(position.returnY() < -20){
            position.writeY(pixelVertical);
        }
    }
    auto [x,y] = position.returnPair();
    Boidshape.setPosition(x,y);
    headingBoid = velocity.heading();
    Boidshape.setRotation(headingBoid);
}


PVector Boid::align(const std::vector<Boid>& BVec){

    PVector Desired(velocity.returnX(),velocity.returnY());
    int total =0;

    for(auto BoidIt:BVec){
        PVector distance(abs(position.returnX()-BoidIt.position.returnX()),abs(position.returnY()-BoidIt.position.returnY()));
        if((distance.returnX() != 0 && distance.returnY() != 0) && (distance.returnX() < perception && distance.returnY() < perception)){
            //Desired.setContent(BoidIt.velocity.returnX()-Desired.returnX(), BoidIt.velocity.returnY()-Desired.returnY());
            Desired.addContent(BoidIt.velocity.returnX(), BoidIt.velocity.returnY());
            total = total+1;
        }
    }
    
    
    
    //if((Desired.returnX() != velocity.returnX()) || (Desired.returnY() != velocity.returnY())){
    
    if(total > 0){
        Desired/total;
        if(Desired.magnitude() > maxSpeed){
            Desired.setMag(maxSpeed);
        }
        //auto [fx,fy] = limitForce(Desired);
        //Desired.setMag(maxGForce);
        
        return Desired;
    } else {
        return {0.,0.};
    }
}

PVector Boid::cohesion(const std::vector<Boid>& BVec){
    PVector DesiredPos(0.,0.);
    float total = 0.0;
    for(auto BoidIt:BVec){
        PVector distance(abs(position.returnX()-BoidIt.position.returnX()),abs(position.returnY()-BoidIt.position.returnY()));
        if((distance.returnX() != 0 && distance.returnY() != 0) && (distance.returnX() < perception && distance.returnY() < perception)){
            DesiredPos.addContent(BoidIt.position.returnX(),BoidIt.position.returnY());
            total = total+1.;
        }
    }
    
    if(total > 0){
        DesiredPos/total;
        DesiredPos.sub(position);
        /*
        if(DesiredPos.magnitude() > 4){
        DesiredPos.setMag(maxSpeed);
        }
         */
        //DesiredPos.sub(velocity);
        
        //auto [fx,fy] = limitForce(DesiredPos);
        if(DesiredPos.magnitude() > maxSpeed){
            DesiredPos.setMag(maxSpeed);
        }
        
        return DesiredPos;
    } else {
        return{0.,0.};
    }
}

PVector Boid::seperation(const std::vector<Boid>& BVec){
    float perceptionSeperation = 100;
    PVector AvoidingForce(0.,0.);
    float total = 0.0;
    
    for(auto BoidIt:BVec){
        PVector distance(abs(position.returnX()-BoidIt.position.returnX()),abs(position.returnY()-BoidIt.position.returnY()));
        if((distance.returnX() != 0 && distance.returnY() != 0) && (distance.returnX() < perceptionSeperation && distance.returnY() < perceptionSeperation)){
            float LengthForce = (perception-distance.magnitude());
            PVector Temp(position.returnX()-BoidIt.position.returnX(),position.returnY()-BoidIt.position.returnY());
            Temp.setMag(LengthForce);
            AvoidingForce.addContent(Temp.returnX(),Temp.returnY());
            
            /*PVector Differenz((position.returnX()-BoidIt.position.returnX()),(position.returnY()-BoidIt.position.returnY()));
            Differenz/(distance.magnitude());
            AvoidingForce.add(Differenz);*/
            total = total+1.;
        }
    }
    
    
    
    if(total > 0){
        AvoidingForce/total;
        /*
        if(AvoidingForce.magnitude() > 6){
        AvoidingForce.setMag(6);
        }
         */
        /*
        AvoidingForce.sub(velocity);
        AvoidingForce.setMag(maxGForce);*/
        //auto [fx,fy] = limitForce(AvoidingForce);
        if(AvoidingForce.magnitude() > maxSpeed+1.5){
            AvoidingForce.setMag(maxSpeed+1.5);
        }
        
        return AvoidingForce;
    } else {
        return {0.0,0.0};
    }
}

PVector Boid::evasion(const std::vector<Obstacle>& OVec){
    PVector AvoidingForce(0.,0.);
    PVector Temp(0.,0.);
    float total = 0.0;
    
    /*
    for(auto ObstIt:OVec){
        PVector Boid2Obst(ObstIt.returnPosX()-position.returnX(), ObstIt.returnPosY()-position.returnY());
        PVector distance(abs(position.returnX()-ObstIt.returnPosX()),abs(position.returnY()-ObstIt.returnPosY()));
        
        if(distance.returnX() < perception && distance.returnY() < perception){
            float AoD = AvoidingForce.AoD(velocity, Boid2Obst)*(180/M_PI);
            //std::cout << "Winkel zwischen Geschwindigkeit Boid und Obstacle Vektor: " << AoD << std::endl;
            
            if(AoD < 90){
                
                float LengthForce = (perception-distance.magnitude());
                PVector Temp(position.returnX()-ObstIt.returnPosX(), position.returnY()-ObstIt.returnPosY());
                Temp.setMag(LengthForce);
                AvoidingForce.addContent(Temp.returnX(),Temp.returnY());
            
                total = total+1.;
            }
        }
    }*/
    
    
    //--> Funktioniert
    
    for(auto ObstIt:OVec){
        PVector Obst2Boids(position.returnX()-(ObstIt.returnPosX()+10.0),position.returnY()-(ObstIt.returnPosY()+10.0));
        PVector distance(abs(position.returnX()-(ObstIt.returnPosX()+10.0)),abs(position.returnY()-(ObstIt.returnPosY()+10.0)));
        
        if(distance.returnX() < perception && distance.returnY() < perception){
            float AoD = AvoidingForce.AoD(velocity, Obst2Boids)*(180/M_PI);
            //std::cout << "Der winkel zwischen geschwindigkeit und Vektor auf Boid betraegt: " << AoD << std::endl;
            
            //if(AoD < 175){
                Temp.setContent(Obst2Boids.returnX(), Obst2Boids.returnY());
                Temp.setMag(1.);
                AvoidingForce.addContent((Temp.returnX()*(perception/distance.magnitude())),(Temp.returnY()*(perception/distance.magnitude())));
                total = total+1;
            //} else if(AoD > 175){
                
            //}
        }
    }
    
    //Abstand über Winkelansatz
    
    /*for(auto ObstIt:OVec){
        PVector X(1,0);
        PVector Obst2Boids(position.returnX()-(ObstIt.returnPosX()+10.0),position.returnY()-(ObstIt.returnPosY()+10.0));
        PVector Boids2Obst((ObstIt.returnPosX()+10.0)-position.returnX(),(ObstIt.returnPosY()+10.0)-ObstIt.returnPosY());
        PVector distance(abs(position.returnX()-(ObstIt.returnPosX()+10.0)),abs(position.returnY()-(ObstIt.returnPosY()+10.0)));
        
        
        
        if(distance.returnX() < perception && distance.returnY() < perception){
            
            float beta = Obst2Boids.AoD(Obst2Boids,X)*(180/M_PI);
            float alpha = Obst2Boids.AoD(velocity,X)*(180/M_PI);
            std::cout << "alpha: " << alpha << " und beta: " << beta << std::endl;
            
            Obst2Boids.setMag(perception);
        
            if(Obst2Boids.returnY() < 0){
                if((velocity.returnY() > 0 && alpha < 180-beta) || (velocity.returnY() < 0 && alpha < beta)){
                    AvoidingForce.addContent(Boids2Obst.returnX()+(cos(90*(M_PI/180))*Obst2Boids.returnY()+sin(90*(M_PI/180))*Obst2Boids.returnX()), Boids2Obst.returnY()+(-1*sin(90*(M_PI/180))*Obst2Boids.returnY()+cos(90*(M_PI/180))*Obst2Boids.returnX()));
                } else{
                    AvoidingForce.addContent(Boids2Obst.returnX()+(cos(90*(M_PI/180))*Obst2Boids.returnY()-sin(90*(M_PI/180))*Obst2Boids.returnX()), Boids2Obst.returnY()+(sin(90*(M_PI/180))*Obst2Boids.returnY()+cos(90*(M_PI/180))*Obst2Boids.returnX()));
                }
            }else if(Obst2Boids.returnY() > 0){
                if((velocity.returnY() < 0 && alpha < (180-beta))||(velocity.returnY() > 0 && alpha < beta)){
                    AvoidingForce.addContent(Boids2Obst.returnX()+(cos(90*(M_PI/180))*Obst2Boids.returnY()-sin(90*(M_PI/180))*Obst2Boids.returnX()), Boids2Obst.returnY()+(sin(90*(M_PI/180))*Obst2Boids.returnY()+cos(90*(M_PI/180))*Obst2Boids.returnX()));
                } else {
                AvoidingForce.addContent(Boids2Obst.returnX()+(cos(90*(M_PI/180))*Obst2Boids.returnY()+sin(90*(M_PI/180))*Obst2Boids.returnX()), Boids2Obst.returnY()+(-1*sin(90*(M_PI/180))*Obst2Boids.returnY()+cos(90*(M_PI/180))*Obst2Boids.returnX()));
                }
            }
            total++;
        }
    }*/
    
    //Abstand über Schnittpunkttheorie
    /*
    for(auto ObstIt:OVec){
        
        PVector Obst2Boids(position.returnX()-(ObstIt.returnPosX()+10.0),position.returnY()-(ObstIt.returnPosY()+10.0));
        PVector Boids2Obst((ObstIt.returnPosX()+10.0)-position.returnX(),(ObstIt.returnPosY()+10.0)-ObstIt.returnPosY());
        PVector distance(abs(position.returnX()-(ObstIt.returnPosX()+10.0)),abs(position.returnY()-(ObstIt.returnPosY()+10.0)));
        float angleDiff;
        float alpha;
        float beta;
        float x;
        float turnAngleLimit = 45;
        
        
        if(distance.returnX() < perception && distance.returnY() < perception){
            angleDiff = velocity.AoD(velocity, Boids2Obst)*(180/M_PI);
            std::cout << "Winkeldifferenz: " << angleDiff << std::endl;
            
            if(angleDiff < turnAngleLimit){
                x = (ObstIt.returnPosY()-position.returnX())/(velocity.returnY()/velocity.returnX());
                if(position.returnY() < ObstIt.returnPosY()){
                    if(x > ObstIt.returnPosX()){
                        alpha = turnAngleLimit;
                    }else if(x < ObstIt.returnPosX()){
                        alpha = -turnAngleLimit;
                    }
                }else if(position.returnY() > ObstIt.returnPosY()){
                    if(x > ObstIt.returnPosX()){
                        alpha = -turnAngleLimit;
                    }else if(x < ObstIt.returnPosX()){
                        alpha = turnAngleLimit;
                    }
                }
            }
            total++;
            if(alpha > 0){
                beta = alpha - angleDiff;
            }else if(alpha < 0){
                beta = alpha + angleDiff;
            }
            AvoidingForce.addContent(velocity.returnX()*cos(beta*(M_PI/180))-velocity.returnY()*sin(beta*(M_PI/180)),velocity.returnX()*sin(beta*(M_PI/180))+velocity.returnY()*cos(beta*(M_PI/180)));
            
        }
    }
    */
    
    //Heading Ansatz#
    /*
    for(auto ObstIt:OVec){
        
        PVector Obst2Boids(position.returnX()-(ObstIt.returnPosX()+10.0),position.returnY()-(ObstIt.returnPosY()+10.0));
        PVector Boids2Obst((ObstIt.returnPosX()+10.0)-position.returnX(),(ObstIt.returnPosY()+10.0)-position.returnY());
        PVector distance(abs(position.returnX()-(ObstIt.returnPosX()+10.0)),abs(position.returnY()-(ObstIt.returnPosY()+10.0)));
        
        if(distance.returnX() < perception && distance.returnY() < perception){
            float angleDiff = velocity.AoD(velocity, Boids2Obst)*(180/M_PI);
            float phi;
            
            //std::cout << "angle Difference beträgt: " << angleDiff << " und der B2O Vekor, x:" << Boids2Obst.returnX() << " y: " << Boids2Obst.returnY() << " und velocity, x: " << velocity.returnX() << " y: " << velocity.returnY() << std::endl;
            
            if(angleDiff < 45){
                float B2OHeading = Boids2Obst.heading();
                float XS = (ObstIt.returnPosY()-position.returnY())/(velocity.returnY()/velocity.returnX());
                float turnAngle = 45; //45;
                std::cout << "avoiding force wird berechnet und Heading beträgt: " << B2OHeading << std::endl;
                
                //1. Quadrant
                if(B2OHeading >= 180 && B2OHeading < 270){
                    if(XS > ObstIt.returnPosX()){
                        phi = turnAngle;
                    }else if(XS < ObstIt.returnPosX()){
                        phi = -turnAngle;
                    }
                }
                //2. Quadrant
                if(B2OHeading >= 90 && B2OHeading < 180){
                    if(XS > ObstIt.returnPosX()){
                        phi = turnAngle;
                    }else if(XS < ObstIt.returnPosX()){
                        phi = -turnAngle;
                    }
                }
                //3. Quadrant
                if(B2OHeading >= 0 && B2OHeading < 90){
                    //if(B2OHeading >= 90 && B2OHeading < 180){
                        if(XS > ObstIt.returnPosX()){
                            phi = -turnAngle;
                        }else if(XS < ObstIt.returnPosX()){
                            phi = turnAngle;
                        }
                    //}
                }
                //4. Quadrant
                if(B2OHeading >= 270 && B2OHeading < 360){
                    //if(B2OHeading >= 90 && B2OHeading < 180){
                        if(XS > ObstIt.returnPosX()){
                            phi = -turnAngle;
                        }else if(XS < ObstIt.returnPosX()){
                            phi = turnAngle;
                        }
                    //}
                }
                PVector temp;
                
            temp.setContent((velocity.returnX()*cos(phi*(M_PI/180))-velocity.returnY()*sin(phi*(M_PI/180))),-1*(velocity.returnX()*sin(phi*(M_PI/180))+velocity.returnY()*cos(phi*(M_PI/180))));
                //selbst hergeleitete 2D Vektorrotation:
                //temp.setContent((velocity.returnX()*cos(phi*(M_PI/180))+velocity.returnY()*sin(phi*(M_PI/180))),(velocity.returnY()*cos(phi*(M_PI/180))+velocity.returnX()*sin(phi*(M_PI/180))));
                
                float strength = perception-Obst2Boids.magnitude();
                //std::cout << "TempX: " << temp.returnX() << " tempY: " << temp.returnY() << " und velX: " << velocity.returnX() << " velY: " << velocity.returnY() << std::endl;
                //--> temp zeigt die gleiche geschwindigkeit wie velocity an, drehenung des vektors scheint nicht zu erfolgen!!!!!!
                
                /*
                if(0 <= strength && strength < perception/4){
                    temp.setMag(0.5*maxSpeed);
                }else if(perception/4 <= strength && strength < perception/2){
                    temp.setMag(maxSpeed);
                }else if(perception/2 <= strength && strength < (3/4)*perception){
                    temp.setMag(1.5*maxSpeed);
                }else if(strength > (3/4)*perception){
                    temp.setMag(2*maxSpeed);
                }
     
                //temp.setMag(strength);
                AvoidingForce.addContent(temp.returnX(), temp.returnY());
                
                
                total++;
            }
        }
    }
*/
    
    /* //Funktioniert nur für einzelnde Hindernisse und anscheinend nicht für einen Schwarm
    for(auto ObstIt:OVec){
        
        PVector Obst2Boids(position.returnX()-(ObstIt.returnPosX()+10.0),position.returnY()-(ObstIt.returnPosY()+10.0));
        PVector Boids2Obst((ObstIt.returnPosX()+10.0)-position.returnX(),(ObstIt.returnPosY()+10.0)-position.returnY());
        PVector distance(abs(position.returnX()-(ObstIt.returnPosX()+10.0)),abs(position.returnY()-(ObstIt.returnPosY()+10.0)));
        
        if(distance.returnX() < perception && distance.returnY() < perception){
            float angleDiff = velocity.AoD(velocity, Boids2Obst)*(180/M_PI);
            
            
            
            if(angleDiff < 45){
                std::cout << "Winkeldifferenz unter 45 Grad" << std::endl;
                float atanHeading = atan2(velocity.returnY(),velocity.returnX());
                float n = position.returnY()-(position.returnX()*(velocity.returnY()/velocity.returnX()));
                float XS = (ObstIt.returnPosY()-n)/(velocity.returnY()/velocity.returnX());
                float phi;
                std::cout << "Die X Pos Boid: " << position.returnX() << "Die X Position des Hindernisses ist: " << ObstIt.returnPosX() << " und der Schnittpunkt mit GeschwVektor: " << XS << std::endl;
                
                // 1. Quadrant
                if(Obst2Boids.returnX() > 0 && Obst2Boids.returnY() <= 0){
                    std::cout << "Boid im 1. Quadrant" << std::endl;
                    if(180 <= atanHeading && atanHeading > 90){
                        if(XS >= ObstIt.returnPosX()){
                            phi = 45;
                        }else if(XS < ObstIt.returnPosX()){
                            phi = -45;
                        }
                    }else if(atanHeading < 0){
                        phi = -45;
                    }
                // 2. Quadrant
                }else if(Obst2Boids.returnX() <= 0 && Obst2Boids.returnY() < 0){
                    std::cout << "Boid im 2. Quadrant" << std::endl;
                    if(90 <= atanHeading && atanHeading > 0){
                        if(XS >= ObstIt.returnPosX()){
                            phi = 45;
                        }else if(XS < ObstIt.returnPosX()){
                            phi = -45;
                        }
                    }else if(atanHeading < 0){
                        phi = 45;
                    }
                // 3. Quadrant
                }else if(Obst2Boids.returnX() < 0 && Obst2Boids.returnY() >= 0){
                    std::cout << "Boid im 3. Quadrant" << std::endl;
                    if(atanHeading <= 0 && atanHeading > -90){
                        if(XS >= ObstIt.returnPosX()){
                            phi = -45;
                        }else if(XS < ObstIt.returnPosX()){
                            phi = 45;
                        }
                    }else if(atanHeading > 0){
                        phi = -45;
                    }
                // 4. Quadrant
                }else if(Obst2Boids.returnX() >= 0 && Obst2Boids.returnY() > 0){
                    std::cout << "Boid im 4. Quadrant" << std::endl;
                    if(atanHeading <= -90 && atanHeading > -180){
                        if(XS >= ObstIt.returnPosX()){
                            phi = -45;
                        }else if(XS < ObstIt.returnPosX()){
                            phi = 45;
                        }
                    }else if(atanHeading > 0){
                        phi = 45;
                    }
                }
                
            Temp.setContent((velocity.returnX()*cos(phi*(M_PI/180))-velocity.returnY()*sin(phi*(M_PI/180))),-1*(velocity.returnX()*sin(phi*(M_PI/180))+velocity.returnY()*cos(phi*(M_PI/180))));
                
                std::cout << "VelX: " << velocity.returnX() << " VelY: " << velocity.returnY() << " TempX: " << Temp.returnX() << " TempY: " << Temp.returnY() << std::endl;
                //Magnitude anpassen
                
                AvoidingForce.addContent(Temp.returnX(), Temp.returnY());
                total++;
            }
        }
    }
*/

    if(total > 0){
        AvoidingForce/total;
        AvoidingForce.setContent(AvoidingForce.returnX(), AvoidingForce.returnY());
        if(AvoidingForce.magnitude() > maxSpeed){
            AvoidingForce.setMag(maxSpeed);
        }
        /*
        if(AvoidingForce.magnitude() > 4){
            AvoidingForce.setMag(4);
            std::cout << "Avoiding Force x: " << AvoidingForce.returnX() << " y: " << AvoidingForce.returnY() << std::endl;
            //std::cout << "Vx: " << AvoidingForce.returnX() << " und Vy: " << AvoidingForce.returnY() << std::endl;
        }
         */
        //std::cout << "AVOIDING FORCE HAS BEEN CALCULATED, X:" << AvoidingForce.returnX() << " Y: " << AvoidingForce.returnY() << std::endl;
        return AvoidingForce;
    } else {
        return {0.0,0.0};
    }
}

PVector Boid::returnDesiredPheromonDirection(){
    int zaehlerI=0;
    int zaehlerJ=0;
    int tempI=0;
    int tempJ=0;
    int maxJatI=0;
    int maxIatJ=0;
    
    //identifies the biggest horizontal line of missing pheromon
    for(int i=position.returnX()-(pheromonPerception/2);i<(position.returnX()+(pheromonPerception/2));i++){
        for(int j=position.returnY()-(pheromonPerception/2);j<position.returnY()+(pheromonPerception/2);j++){
            if(i >=0 && i <= pixelHorizontal && j >= 0 && j <= pixelVertical){
                if(pheromonArray[i][j] == 0){
                    zaehlerJ++;
                }else if(pheromonArray[i][j] > 0){
                    if(zaehlerJ > tempJ){
                        tempJ=zaehlerJ;
                        maxJatI=i;
                    }
                    zaehlerJ=0;
                }
                if(tempJ == pixelVertical){
                    std::cout << "Es handelt sich um eine Nullspalte bei x: " << maxJatI << std::endl;
                }
            }
        }
    }
    //identifies the biggest vertical line of missing pheromon
    for(int j=position.returnY()-(pheromonPerception/2);j<position.returnY()+(pheromonPerception/2);j++){
        for(int i=position.returnX()-(pheromonPerception/2);i<(position.returnX()+(pheromonPerception/2));i++){
            if(i >=0 && i <= pixelHorizontal && j >= 0 && j <= pixelVertical){
                if(pheromonArray[i][j] == 0){
                    zaehlerI++;
                }else if(pheromonArray[i][j] > 0){
                    if(zaehlerI > tempI){
                        tempI=zaehlerI;
                        maxIatJ=j;
                    }
                    zaehlerI=0;
                }
            }
        }
    }
    if(zaehlerI !=0 && zaehlerJ !=0){
        PVector desiredPos;
        desiredPos.setContent(maxJatI,maxIatJ);
        PVector desiredDircetion(desiredPos.returnX()-position.returnX(),desiredPos.returnY()-position.returnY());
    
        if(desiredDircetion.magnitude() > maxSpeed){
            desiredDircetion.setMag(maxSpeed);
        }
    
        return desiredDircetion;
    }else{
        return {0.0,0.0};
        std::cout << "Es wurde keine Pheromon Position berechnet" << std::endl;
    }
}

PVector Boid::pheromonDirectionByGradient(){
    PVector Desired(0.,0.);
    PVector Temp(velocity.returnX(),velocity.returnY());
    PVector originalTemp(velocity.returnX(),velocity.returnY());
    Temp.setMag(perception+1);
    float tempPheromon=pheromonStrength;
    std::vector<float> possibleDegrees;
    bool localPheromon=false;
    
    for(float degree=-135;degree <= 135;degree=degree+0.5){
        
        //std::cout << "Temp vor dem drehen: " << Temp.returnX() << " " << Temp.returnY() << std::endl;
        //Temp.rotate(degree);
        //std::cout << "Temp nach der Drehung: " << Temp.returnX() << " " << Temp.returnY() << std::endl;
        PVector DegVec = rotate(Temp, degree);
        
        int x = round(position.returnX()+DegVec.returnX());
        int y = round(position.returnY()+DegVec.returnY());
        
        /*
        if(x < 0){
            x = 1; //x = 0;
        }
        if(x > pixelHorizontal){
            //x = pixelHorizontal;
            x = pixelHorizontal-1;
        }
        if(y < 0){
            y = 1; //y = 0;
        }
        if(y > pixelVertical){
            //y = pixelVertical;
            y= pixelVertical-1;
        }
        */
        
        /*
        if(pheromonArray[x][y] == 0){
            //Desired.setContent(Temp.returnX(), Temp.returnY());
            tempPheromon = pheromonArray[x][y];
            possibleDegrees.push_back(degree);
            localPheromon=true;
        }
         */
        if((x > 0 && x < pixelHorizontal) && (y > 0 && y < pixelVertical)){
            if(pheromonArray[x][y] < tempPheromon){
                tempPheromon = pheromonArray[x][y];
                possibleDegrees.clear();
                
                possibleDegrees.push_back(degree);
                localPheromon=true;
            } else if(pheromonArray[x][y] == tempPheromon && tempPheromon < pheromonStrength){
                possibleDegrees.push_back(degree);
                localPheromon=true;
            }
        }
    }
    
    
    if(possibleDegrees.size() > 0){
        originalTemp.rotate(possibleDegrees[std::rand()%possibleDegrees.size()]);
        Desired.add(originalTemp);
    }
    
    if(!localPheromon){
        //std::cout << "Im Umkreis ist kein Pheromon zu finden";
        float dist2Phero= 2000;
        float lowestPheroStrength=pheromonStrength-1;
        int xTemp;
        int yTemp;
        
        for(int x=0; x < pixelHorizontal; x++){
            for(int y=0; y < pixelVertical; y++){
                if(pheromonArray[x][y] <= lowestPheroStrength){
                    PVector distance(abs(x-position.returnX()),abs(y-position.returnY()));
                    if(distance.magnitude() < dist2Phero){
                        lowestPheroStrength = pheromonArray[x][y];
                        dist2Phero=distance.magnitude();
                        xTemp=x;
                        yTemp=y;
                    }
                }
            }
        }
        
        Desired.setContent(xTemp-position.returnX(), yTemp-position.returnY());
        //std::cout << " und die Distanz zum nächsten Suchpunkt: " << dist2Phero << std::endl;
    }
    
    if(boundariesActive){
        PVector avoidingForce = stayInBoundaries();
        avoidingForce/2;
        Desired.add(avoidingForce);
    }
    
    if(Desired.magnitude() > maxSpeed){
        Desired.setMag(maxSpeed);
    }
    //std::cout << "Der Vektor Pheromon berechnet: " << Desired.returnX() << " " << Desired.returnY() << std::endl;
    
    return Desired;
}

PVector Boid::searchVonoriCells(){
    PVector Desired(0.,0.);
    PVector Temp(velocity.returnX(),velocity.returnY());
    Temp.setMag(perception);
    PVector originalTemp(velocity.returnX(),velocity.returnY());
    Temp.setMag(perception+1);
    float tempPheromon=pheromonStrength;
    std::vector<float> possibleDegrees;
    bool localPheromon;
    sf::Color vonoriColorBoid = getVonoriCellCollorOfBoid();
    
    //std::cout << "Der Boid soll sich nur auf folgender Farbe bewegen: " << static_cast<int>(vonoriColorBoid.r) << " " << static_cast<int>(vonoriColorBoid.g) << " " << static_cast<int>(vonoriColorBoid.b) << " und die Farbe an der eigenen Position: " << static_cast<int>(vonoriVertexArray[round(position.returnX())+round(position.returnY())*pixelHorizontal].color.r) << " " << static_cast<int>(vonoriVertexArray[round(position.returnX())+round(position.returnY())*pixelHorizontal].color.g) << " " << static_cast<int>(vonoriVertexArray[round(position.returnX())+round(position.returnY())*pixelHorizontal].color.b) << " und die aktuelle Position ist: " << position.returnX() << " " << position.returnY() << std::endl;
    
    if(static_cast<int>(vonoriVertexArray[round(position.returnX())+round(position.returnY())*pixelHorizontal].color.r) != static_cast<int>(vonoriColorBoid.r) || static_cast<int>(vonoriVertexArray[round(position.returnX())+round(position.returnY())*pixelHorizontal].color.g) != static_cast<int>(vonoriColorBoid.g) || static_cast<int>(vonoriVertexArray[round(position.returnX())+round(position.returnY())*pixelHorizontal].color.b) != static_cast<int>(vonoriColorBoid.b)){
        
        std::cout << "Boid hat seine Zelle verlassen" << std::endl;
        
        //kürzeste Distanz zurück ins eigene Feld
        float shortestDistance=800;
        for(int i=0;i<pixelHorizontal-1;i++){
            for(int j=0;j<pixelVertical-1;j++){
                if(static_cast<int>(vonoriVertexArray[i+j*pixelHorizontal].color.r) == static_cast<int>(vonoriColorBoid.r) && static_cast<int>(vonoriVertexArray[i+j*pixelHorizontal].color.g) == static_cast<int>(vonoriColorBoid.g) && static_cast<int>(vonoriVertexArray[i+j*pixelHorizontal].color.b) == static_cast<int>(vonoriColorBoid.b)){
                    float tempDist = sqrt(pow(i-position.returnX(),2)+pow(j-position.returnY(),2));
                    
                    if(tempDist < shortestDistance){
                        shortestDistance=tempDist;
                        Desired.setContent(i-position.returnX(), j-position.returnY());
                    }
                }
            }
        }
            
    }else{
    
        for(float degree=-135;degree <= 135;degree=degree+0.5){
            
            PVector DegVec = rotate(Temp, degree);
            
            int x = round(position.returnX()+DegVec.returnX());
            int y = round(position.returnY()+DegVec.returnY());
            
            if((x > 0 && x < pixelHorizontal) && (y > 0 && y < pixelVertical) && (static_cast<int>(vonoriVertexArray[x+y*pixelHorizontal].color.r) == static_cast<int>(vonoriColorBoid.r) && static_cast<int>(vonoriVertexArray[x+y*pixelHorizontal].color.g) == static_cast<int>(vonoriColorBoid.g) && static_cast<int>(vonoriVertexArray[x+y*pixelHorizontal].color.b) == static_cast<int>(vonoriColorBoid.b))){
                
                if(pheromonArray[x][y] < tempPheromon){
                    tempPheromon = pheromonArray[x][y];
                    possibleDegrees.clear();
                    
                    possibleDegrees.push_back(degree);
                    localPheromon=true;
                } else if(pheromonArray[x][y] == tempPheromon && tempPheromon < pheromonStrength){
                    possibleDegrees.push_back(degree);
                    localPheromon=true;
                }
            }
        }
        
        if(possibleDegrees.size() > 0){
            originalTemp.rotate(possibleDegrees[std::rand()%possibleDegrees.size()]);
            Desired.add(originalTemp);
        }
        
        /*
        if(!localPheromon){
            float dist2Phero= 2000;
            float lowestPheroStrength=pheromonStrength-1;
            int xTemp;
            int yTemp;
            
            for(int x=0; x < pixelHorizontal; x++){
                for(int y=0; y < pixelVertical; y++){
                    if(pheromonArray[x][y] <= lowestPheroStrength){
                        PVector distance(abs(x-position.returnX()),abs(y-position.returnY()));
                        if(distance.magnitude() < dist2Phero && (vonoriVertexArray[x+y*pixelHorizontal].color.r == vonoriColorBoid.r && vonoriVertexArray[x+y*pixelHorizontal].color.g == vonoriColorBoid.g && vonoriVertexArray[x+y*pixelHorizontal].color.b == vonoriColorBoid.b)){
                            
                            lowestPheroStrength = pheromonArray[x][y];
                            dist2Phero=distance.magnitude();
                            xTemp=x;
                            yTemp=y;
                        }
                    }
                }
            }
             Desired.setContent(xTemp-position.returnX(), yTemp-position.returnY());
        }*/
    }
    
    if(boundariesActive){
           PVector avoidingForce = stayInBoundaries();
           avoidingForce/2;
           Desired.add(avoidingForce);
    }
    
    if(Desired.magnitude() > maxSpeed){
        Desired.setMag(maxSpeed);
    }
    
    return Desired;
    
}

PVector Boid::stayInBoundaries(){
    PVector Desired(0,0);
    
    if(position.returnX() < 100){
        float xScaled = 100-position.returnX();
        if(headingBoid < -88 && headingBoid > -92){
            Desired.addContent(xScaled, 2);
        }else{
            Desired.addContent(xScaled,0);
        }
    }
    if(position.returnX() > pixelHorizontal-100){
        float xScaled = (pixelHorizontal-100)-position.returnX();
        if(headingBoid > 88 && headingBoid < 92){
            Desired.addContent(xScaled, 2);
        }else{
            Desired.addContent(xScaled,0);
        }
    }
    if(position.returnY() < 100){
        float yScaled = 100-position.returnY();
        if(headingBoid > -2 && headingBoid < 2){
            Desired.addContent(2, yScaled);
        }else{
            Desired.addContent(0, yScaled);
        }
    }
    if(position.returnY() > pixelVertical-100){
        float yScaled= (pixelVertical-100)-position.returnY();
        if((headingBoid < -178 && headingBoid >= -180) || (headingBoid > 178 && headingBoid <= 180)){
            Desired.addContent(2, yScaled);
        }else{
            Desired.addContent(0, yScaled);
        }
    }
    
    return Desired;
}

PVector Boid::SEAD(std::vector<Target> TVec,const std::vector<Boid>& BVec, std::vector<std::vector<PVector>> *BoidAssigned2Target2D){

    PVector Desired(0.,0.);

    float numberTargets = TVec.size();
    float numberBoids = BVec.size();
    float maxNumberBoidsPerTarget = ceil((numberBoids/numberTargets));
    float distance = 2000;
    int closesTargetNumber=0;
    std::vector<std::vector<float>> targetDistance;
    
    for(int i=0;i<numberTargets;i++){
        PVector B2T(TVec.at(i).returnTargetPos().returnX()-position.returnX(),TVec.at(i).returnTargetPos().returnY()-position.returnY());
        targetDistance.push_back(std::vector<float>(1,i));
        targetDistance[i].push_back(B2T.magnitude());
    }
    for(int i=0;i< targetDistance.size();i++){
        for(int j=0;j<targetDistance.size()-1;j++){
            if(targetDistance[j][1] > targetDistance[j+1][1]){
                float tempTargetNumber = targetDistance[j][0];
                float tempTargetDistance = targetDistance[j][1];
                targetDistance[j][0] = targetDistance[j+1][0];
                targetDistance[j][1] = targetDistance[j+1][1];
                targetDistance[j+1][0] = tempTargetNumber;
                targetDistance[j+1][1] = tempTargetDistance;
            }
        }
    }
    bool noTargetassigned = true;
    int counter = 0;
    PVector currentTargetPos;
    while(noTargetassigned && (counter < targetDistance.size())){
        
        if(BoidAssigned2Target2D->at(targetDistance[counter][0]).size() < maxNumberBoidsPerTarget+1){
            BoidAssigned2Target2D->at(targetDistance[counter][0]).push_back(position);
            PVector Temp(TVec.at(targetDistance[counter][0]).returnTargetPos().returnX()-position.returnX(),TVec.at(targetDistance[counter][0]).returnTargetPos().returnY()-position.returnY());
            currentTargetPos.setContent(TVec.at(targetDistance[counter][0]).returnTargetPos().returnX(), TVec.at(targetDistance[counter][0]).returnTargetPos().returnY());
            if(Temp.magnitude() < 100){
                Temp.setMag(maxSpeed);
                Temp.rotate(10);
                Desired.add(Temp);
            }else{
                Temp.setMag(maxSpeed);
                Desired.add(Temp);
            }
            noTargetassigned = false;
        }
        counter++;
    }
    
    PVector averagePosBoids(0.,0.);
    int boidsInRange= 0;
    for(auto BoidIt:BVec){
        PVector Temp(position.returnX()-BoidIt.returnPosBoid().returnX(),position.returnY()-BoidIt.returnPosBoid().returnY());
        if(Temp.magnitude() < perception){
            PVector BIt2T(currentTargetPos.returnX()-BoidIt.returnPosBoid().returnX(),currentTargetPos.returnY()-BoidIt.returnPosBoid().returnY());
            
            if((180/M_PI)*Temp.AoD(BIt2T, BoidIt.returnVelocityBoid()) < 15){
                averagePosBoids.addContent(BoidIt.returnPosBoid().returnX(), BoidIt.returnPosBoid().returnY());
                boidsInRange++;
            }
        }
    }
    if(boidsInRange > 0){
        averagePosBoids/boidsInRange;
    
        PVector TB(position.returnX()-currentTargetPos.returnX(),position.returnY()-currentTargetPos.returnY());
        PVector TavB(averagePosBoids.returnX()-currentTargetPos.returnX(),averagePosBoids.returnY()-currentTargetPos.returnY());
        float angleBetweenVectors = TB.AoD(TB, TavB);
        float boid2TargetDistance = TB.magnitude();
        float h = boid2TargetDistance*sin(angleBetweenVectors);
        float c1 = sqrt(pow(boid2TargetDistance,2)-pow(h,2));
        if(angleBetweenVectors > 0.0001 && TB.magnitude() > 100){
            PVector TC1(averagePosBoids.returnX()-currentTargetPos.returnX(),averagePosBoids.returnY()-currentTargetPos.returnY());
            TC1.setMag(c1);
            PVector P(TC1.returnX()+currentTargetPos.returnX(),TC1.returnY()+currentTargetPos.returnY());
            PVector P2Av(averagePosBoids.returnX()-P.returnX(),averagePosBoids.returnY()-P.returnY());
            if(P2Av.magnitude() > 0.0001){
                Desired.add(P2Av);
            }
        }
    }
    PVector AvoidingForce(0.,0.);
    float total = 0.0;
    
    for(auto BoidIt:BVec){
        PVector distance(abs(position.returnX()-BoidIt.position.returnX()),abs(position.returnY()-BoidIt.position.returnY()));
        if((distance.returnX() != 0 && distance.returnY() != 0) && (distance.returnX() < perception && distance.returnY() < perception)){
            float LengthForce = (perception-distance.magnitude());
            PVector Temp(position.returnX()-BoidIt.position.returnX(),position.returnY()-BoidIt.position.returnY());
            Temp.setMag(LengthForce);
            AvoidingForce.addContent(Temp.returnX(),Temp.returnY());
            total = total+1.;
        }
    }
    
    if(total > 0){
        AvoidingForce/total;
        if(AvoidingForce.magnitude() > maxSpeed){
            AvoidingForce.setMag(maxSpeed-0.5);
        }
        Desired.add(AvoidingForce);
    }
    
    
    if(Desired.magnitude() > maxSpeed){
        Desired.setMag(maxSpeed);
    }
    
    return Desired;
}


PVector Boid::Attack(const std::vector<Boid>& BVec, std::vector<Target>& TVec,  std::vector<std::vector<PVector>> *BoidAssigned2Target2D){
    PVector Desired(0.,0.);

    float numberTargets = TVec.size();
    float numberBoids = BVec.size();
    float maxNumberBoidsPerTarget = ceil((numberBoids/numberTargets));
    float distance = 2000;
    int closesTargetNumber=0;
    std::vector<std::vector<float>> targetDistance;
    
    for(int i=0;i<numberTargets;i++){
        PVector B2T(TVec.at(i).returnTargetPos().returnX()-position.returnX(),TVec.at(i).returnTargetPos().returnY()-position.returnY());
        targetDistance.push_back(std::vector<float>(1,i));
        targetDistance[i].push_back(B2T.magnitude());
    }
    for(int i=0;i< targetDistance.size();i++){
        for(int j=0;j<targetDistance.size()-1;j++){
            if(targetDistance[j][1] > targetDistance[j+1][1]){
                float tempTargetNumber = targetDistance[j][0];
                float tempTargetDistance = targetDistance[j][1];
                targetDistance[j][0] = targetDistance[j+1][0];
                targetDistance[j][1] = targetDistance[j+1][1];
                targetDistance[j+1][0] = tempTargetNumber;
                targetDistance[j+1][1] = tempTargetDistance;
            }
        }
    }
    bool noTargetassigned = true;
    int counter = 0;
    PVector currentTargetPos;
    while(noTargetassigned && (counter < targetDistance.size())){
        
        if(BoidAssigned2Target2D->at(targetDistance[counter][0]).size() < maxNumberBoidsPerTarget+1){
            BoidAssigned2Target2D->at(targetDistance[counter][0]).push_back(position);
            PVector Temp(TVec.at(targetDistance[counter][0]).returnTargetPos().returnX()-position.returnX(),TVec.at(targetDistance[counter][0]).returnTargetPos().returnY()-position.returnY());
            currentTargetPos.setContent(TVec.at(targetDistance[counter][0]).returnTargetPos().returnX(), TVec.at(targetDistance[counter][0]).returnTargetPos().returnY());
            if(Temp.magnitude() < 200){
                Temp.setMag(maxSpeed);
                Temp.rotate(10);
                Desired.add(Temp);
            }else{
                Temp.setMag(maxSpeed);
                Desired.add(Temp);
            }
            noTargetassigned = false;
        }
        counter++;
    }
    
    PVector averagePosBoids(0.,0.);
    int boidsInRange= 0;
    for(auto BoidIt:BVec){
        PVector Temp(abs(position.returnX()-BoidIt.returnPosBoid().returnX()),abs(position.returnY()-BoidIt.returnPosBoid().returnY()));
        if(Temp.magnitude() < perception){
            averagePosBoids.addContent(BoidIt.returnPosBoid().returnX(), BoidIt.returnPosBoid().returnY());
            boidsInRange++;
        }
    }
    if(boidsInRange > 0){
        averagePosBoids/boidsInRange;
    
    
        PVector TB(position.returnX()-currentTargetPos.returnX(),position.returnY()-currentTargetPos.returnY());
        PVector TavB(averagePosBoids.returnX()-currentTargetPos.returnX(),averagePosBoids.returnY()-currentTargetPos.returnY());
        float angleBetweenVectors = TB.AoD(TB, TavB);
        float boid2TargetDistance = TB.magnitude();
        float h = boid2TargetDistance*sin(angleBetweenVectors);
        float c1 = sqrt(pow(boid2TargetDistance,2)-pow(h,2));
        if(angleBetweenVectors > 0.0001 && TB.magnitude() > 100){
            PVector TC1(averagePosBoids.returnX()-currentTargetPos.returnX(),averagePosBoids.returnY()-currentTargetPos.returnY());
            TC1.setMag(c1);
            PVector P(TC1.returnX()+currentTargetPos.returnX(),TC1.returnY()+currentTargetPos.returnY());
            PVector BP(P.returnX()-position.returnX(),P.returnY()-position.returnY());
            if(BP.magnitude() > 0.0001){
                BP.setMag(maxSpeed);
                Desired.add(BP);
            }
            //std::cout << "Der Winkel beträgt: " << angleBetweenVectors << " und h hat den Betrag: " << h << " und BP beträgt: " << BP.returnX() << " und " << BP.returnY() << std::endl;
        }
    }
    
    PVector AvoidingForce(0.,0.);
    float total = 0.0;
    for(auto BoidIt:BVec){
        PVector distance(abs(position.returnX()-BoidIt.position.returnX()),abs(position.returnY()-BoidIt.position.returnY()));
        if((distance.returnX() != 0 && distance.returnY() != 0) && (distance.returnX() < perception && distance.returnY() < perception)){
            float LengthForce = (perception-distance.magnitude());
            PVector Temp(position.returnX()-BoidIt.position.returnX(),position.returnY()-BoidIt.position.returnY());
            Temp.setMag(LengthForce);
            AvoidingForce.addContent(Temp.returnX(),Temp.returnY());
            total = total+1.;
        }
    }
    if(total > 0){
        AvoidingForce/total;
        if(AvoidingForce.magnitude() > maxSpeed){
            AvoidingForce.setMag(maxSpeed);
        }
        Desired.add(AvoidingForce);
    }
    
    return Desired;
}

PVector Boid::protectAircraft(const std::vector<AircraftFriend> &BAcfVec, const std::vector<AircraftFoe> &FAcfVec){
    PVector Desired(0.,0.);
    
    if(BAcfVec.size() > 0 && FAcfVec.size() > 0){
        std::vector<PVector> protectPositions;
        for(auto FoeIt:FAcfVec){
            for(auto BlueIt:BAcfVec){
                float xPosProtect = FoeIt.returnPosAcf().returnX()-BlueIt.returnPosAcf().returnX();
                float yPosProtect = FoeIt.returnPosAcf().returnY()-BlueIt.returnPosAcf().returnY();
                PVector Directions(xPosProtect,yPosProtect);
                float lengthTempVec = Directions.magnitude();
                Directions.setMag((lengthTempVec/2));
                float x = BlueIt.returnPosAcf().returnX() + Directions.returnX();
                float y = BlueIt.returnPosAcf().returnY() + Directions.returnY();
                PVector Temp(x,y);
                protectPositions.push_back(Temp);
            }
        }
        float distanceProtectionArea = 2000;
        int counter=0;
        for(int i=0;i<protectPositions.size();i++){
            float xPos = protectPositions.at(i).returnX()-position.returnX();
            float yPos = protectPositions.at(i).returnY()-position.returnY();
            PVector protectionOption(xPos,yPos);
            
            if(distanceProtectionArea > protectionOption.magnitude()){
                distanceProtectionArea = protectionOption.magnitude();
                counter = i;
            }
        }
        Desired.setContent(protectPositions.at(counter).returnX()-position.returnX(), protectPositions.at(counter).returnY()-position.returnY());
    }else{
        std::cout << "There is no Blue and Red Force Acf defined" << std::endl;
    }
    if(Desired.magnitude() > maxSpeed){
        Desired.setMag(maxSpeed);
    }
    return Desired;
}

void Boid::assignBoids2Targets(std::vector<Target> TVec, const std::vector<Boid> &BVec, std::vector<std::vector<PVector> > *BoidAssigned2Target2D){
    float numberTargets = TVec.size();
    float numberBoids = BVec.size();
    float maxNumberBoidsPerTarget = ceil((numberBoids/numberTargets));
    float distance = 2000;
    int closesTargetNumber=0;
    std::vector<std::vector<float>> targetDistance;
    
    for(int i=0;i<numberTargets;i++){
        PVector B2T(TVec.at(i).returnTargetPos().returnX()-position.returnX(),TVec.at(i).returnTargetPos().returnY()-position.returnY());
        targetDistance.push_back(std::vector<float>(1,i));
        targetDistance[i].push_back(B2T.magnitude());
    }
    for(int i=0;i< targetDistance.size();i++){
        for(int j=0;j<targetDistance.size()-1;j++){
            if(targetDistance[j][1] > targetDistance[j+1][1]){
                float tempTargetNumber = targetDistance[j][0];
                float tempTargetDistance = targetDistance[j][1];
                targetDistance[j][0] = targetDistance[j+1][0];
                targetDistance[j][1] = targetDistance[j+1][1];
                targetDistance[j+1][0] = tempTargetNumber;
                targetDistance[j+1][1] = tempTargetDistance;
            }
        }
    }
    bool noTargetassigned = true;
    int counter = 0;
    PVector currentTargetPos;
    while(noTargetassigned && (counter < targetDistance.size())){
        
        if(BoidAssigned2Target2D->at(targetDistance[counter][0]).size() < maxNumberBoidsPerTarget+1){
            BoidAssigned2Target2D->at(targetDistance[counter][0]).push_back(position);
            noTargetassigned = false;
        }
        counter++;
    }
}

PVector Boid::rotate(PVector Vec, float degree){
    float rad = M_PI/180 * degree;
    float x = Vec.returnX();
    float y = Vec.returnY();
    x = cos(rad)*x - sin(rad)*y;
    y = sin(rad)*x + cos(rad)*y;
    
    return {x,y};
}

void Boid::setVonoriCellCollorOfBoid(int r, int g, int b){
    
    vonoriCellCollorOfBoid.r = r;
    vonoriCellCollorOfBoid.g = g;
    vonoriCellCollorOfBoid.b = b;
    
}

sf::Color Boid::getVonoriCellCollorOfBoid(){
    return vonoriCellCollorOfBoid;
}


void Boid::flock(std::vector<Boid> *BVec, std::vector<Obstacle> *OVec, std::vector<Target> *TVec, std::vector<AircraftFriend> *BAcfVec, std::vector<AircraftFoe> *FAcfVec,  std::vector<std::vector<PVector>> *BoidAssigned2Target2D) {
    acceleration.setContent(0.,0.);
    bool evadeBoundary=false;
    
    for(int i=0;i< TVec->size();i++){
        PVector B2T(TVec->at(i).returnTargetPos().returnX()-position.returnX(),TVec->at(i).returnTargetPos().returnY()-position.returnY());
        float magnitute = B2T.magnitude();
        if(magnitute < 100.f){
            TVec->at(i).targetDetected();
        }
    }
    
    PVector evade = evasion(*OVec);
    acceleration.addContent(evade.returnX(), evade.returnY());
    
    if(boundariesActive && (!AttackActive || !vonoriActive)){
        PVector boundaryForce = stayInBoundaries();
        acceleration.addContent(boundaryForce.returnX(), boundaryForce.returnY());
        if(boundaryForce.magnitude() > 0){
            evadeBoundary=true;
        }else if(boundaryForce.magnitude() == 0){
            evadeBoundary=false;
        }
    }
    
    if(pheromonActive){
        PVector directionPheromonDesired = pheromonDirectionByGradient(); //returnDesiredPheromonDirection();
        acceleration.addContent(directionPheromonDesired.returnX(), directionPheromonDesired.returnY());
    }
    
    if(jamSamSitesActive){
        PVector toSamSite = SEAD(*TVec,*BVec, BoidAssigned2Target2D);
        acceleration.addContent(toSamSite.returnX(), toSamSite.returnY());
    }
    
    if(AttackActive){
        PVector attackForce = Attack(*BVec, *TVec, BoidAssigned2Target2D);
        acceleration.add(attackForce);
    }
    
    if(vonoriActive){
        PVector vonoriDesired = searchVonoriCells();
        acceleration.add(vonoriDesired);
    }
    
    if((evade.returnX()==0. && evade.returnY() == 0.) && !evadeBoundary && !jamSamSitesActive && !AttackActive){
        PVector seperate = seperation(*BVec);
        acceleration.addContent(seperate.returnX(), seperate.returnY());
    }

    
    if((evade.returnX()==0. && evade.returnY() == 0.) && !pheromonActive && !evadeBoundary && !jamSamSitesActive && !AttackActive && !vonoriActive && !seperationActive){
        PVector alligment = align(*BVec);
        PVector cohesionForce = cohesion(*BVec);
        acceleration.addContent(alligment.returnX(), alligment.returnY());
        acceleration.addContent(cohesionForce.returnX(), cohesionForce.returnY());
    }
    
    if(protectAircraftActive){
        PVector protectForce = protectAircraft(*BAcfVec, *FAcfVec);
        acceleration.addContent(protectForce.returnX(), protectForce.returnY());
    }
    
    if((acceleration.returnX() != 0 || acceleration.returnY() != 0) && acceleration.magnitude() > maxGForce){
        acceleration.setMag(maxGForce);
    }
    
}
    


std::pair<float,float> Boid::limitForce(PVector force){
    
    if(force.magnitude() > maxGForce){
        force.setMag(maxGForce);
    }
    return {force.returnX(),force.returnY()};
}

PVector Boid::returnPosBoid(){
    return position;
}
float Boid::returnPerception(){
    return perception;
}
PVector Boid::returnVelocityBoid(){
    return velocity;
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                        //PHEROMON FUNCTIONS
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

void initPheromon(){
    for(int i=0;i<pheromonArray.size();i++){
        pheromonArray.at(i).fill({});
    }
};

void updatePheromon(PVector BPos,float perception){
    
    //if(BPos.returnX()-perception > 0 && BPos.returnY()-perception > 0 && BPos.returnX()+perception < pixelHorizontal && BPos.returnY()+perception < pixelVertical){
        for(int i = BPos.returnX()-perception;i<BPos.returnX()+perception; i++ ){
            for(int j = BPos.returnY()-perception; j < BPos.returnY()+perception; j++){
                if(sqrt(pow(i-BPos.returnX(),2)+pow(j-BPos.returnY(),2)) < perception && i < pixelHorizontal && j < pixelVertical && i > 0 && j > 0){
                    pheromonArray[i][j]=pheromonStrength;
                }
            }
        }
    
};

void fadingPheromon(int i, int j){
    
            if(pheromonArray[i][j] >= decayRate){
                pheromonArray[i][j]=pheromonArray[i][j]-decayRate;
            }
};

colorStruct setPheromonColour(float i, float j){
    float time = pheromonArray[i][j];
    float r = ((defaultPheromonColourR)/pheromonStrength)*time;
    float g = ((defaultPheromonColourG)/pheromonStrength)*time;
    float b = ((defaultPheromonColourB)/pheromonStrength)*time;
    colorStruct col{r,g,b};
    return col;
};

void initPheromonVertexArray(){
    int pixelTotal=pixelVertical*pixelHorizontal;
    
    for(int i=0;i<pixelTotal;i++){
        pheromonVertexArray[i].color = sf::Color::Black;
    }
}

float progressPheromon(){
    float pixelTotal=pixelVertical*pixelHorizontal;
    float blackPixelCounter=0;
    float result=0;
    
    
    
    for(int i=0;i < pixelTotal;i++){
        
        if(pheromonVertexArray[i].color == sf::Color::Black || (static_cast<float>(pheromonVertexArray[i].color.r) < 2*decayRate && static_cast<float>(pheromonVertexArray[i].color.g) < 2*decayRate && static_cast<float>(pheromonVertexArray[i].color.b) < 2*decayRate) ){
            blackPixelCounter++;
            //std::cout << "Die Farbcodes betragen: " << static_cast<float>(pheromonVertexArray[i].color.r) << " " << static_cast<float>(pheromonVertexArray[i].color.g) << " " << static_cast<float>(pheromonVertexArray[i].color.b) << std::endl;
        }
    }
    
    result = (1 - (blackPixelCounter/pixelTotal))*100;
    
    return result;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                        //Vonori Cells
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


bool noOtherColor(int PosVertex, int size, int boidNumber){
    bool result = false;
    std::vector<sf::Color> colorVec{sf::Color(102,0,0),sf::Color(102,51,0),sf::Color(102,102,0),sf::Color(51,102,0),sf::Color(0,102,102),sf::Color(0,51,102),sf::Color(51,0,102),sf::Color(102,0,102),sf::Color(102,0,51),sf::Color(32,32,32),sf::Color(153,0,0),sf::Color(153,76,0),sf::Color(153,153,0),sf::Color(76,153,0),sf::Color(0,153,76),sf::Color(0,153,153),sf::Color(0,76,153),sf::Color(76,0,153),sf::Color(153,0,153),sf::Color(153,0,76),sf::Color(64,64,64),sf::Color(204,0,0),sf::Color(204,102,0),sf::Color(204,204,0),sf::Color(102,204,0),sf::Color(0,204,102),sf::Color(0,204,204),sf::Color(0,102,204),sf::Color(102,0,204),sf::Color(204,0,204),sf::Color(204,0,102),sf::Color(96,96,96),sf::Color(255,0,0),sf::Color(255,128,0),sf::Color(255,255,0),sf::Color(128,255,0),sf::Color(0,255,128),sf::Color(0,255,255),sf::Color(0,128,255),sf::Color(127,0,255),sf::Color(255,0,255),sf::Color(255,0,127),sf::Color(128,128,128),sf::Color(255,153,51),sf::Color(51,51,255),sf::Color(153,51,255),sf::Color(255,51,153),sf::Color(160,160,160),sf::Color(255,102,102),sf::Color(255,178,102),sf::Color(178,255,102),sf::Color(102,102,255),sf::Color(178,102,255),sf::Color(255,102,255),sf::Color(255,102,178),sf::Color(192,192,192)};
    
    //std::cout << static_cast<int>(vonoriVertexArray[PosVertex].color.r) << std::endl;
    
    for(int i = 0; i<size;i++){
        if(static_cast<int>(vonoriVertexArray[PosVertex].color.r) != static_cast<int>(colorVec[i].r) || static_cast<int>(vonoriVertexArray[PosVertex].color.g) != static_cast<int>(colorVec[i].g) || static_cast<int>(vonoriVertexArray[PosVertex].color.b) != static_cast<int>(colorVec[i].b)){
            result = true;
        }
        if(static_cast<int>(vonoriVertexArray[PosVertex].color.r) == static_cast<int>(colorVec[i].r) && static_cast<int>(vonoriVertexArray[PosVertex].color.g) == static_cast<int>(colorVec[i].g) && static_cast<int>(vonoriVertexArray[PosVertex].color.b) == static_cast<int>(colorVec[i].b) && boidNumber != i){
            result = false;
            return result;
        }
    }
    
    return result;
}

    

void vonoriCells(std::vector<Boid>& BVec, int radius){
    std::vector<sf::Color> colorVec{sf::Color(102,0,0),sf::Color(102,51,0),sf::Color(102,102,0),sf::Color(51,102,0),sf::Color(0,102,102),sf::Color(0,51,102),sf::Color(51,0,102),sf::Color(102,0,102),sf::Color(102,0,51),sf::Color(32,32,32),sf::Color(153,0,0),sf::Color(153,76,0),sf::Color(153,153,0),sf::Color(76,153,0),sf::Color(0,153,76),sf::Color(0,153,153),sf::Color(0,76,153),sf::Color(76,0,153),sf::Color(153,0,153),sf::Color(153,0,76),sf::Color(64,64,64),sf::Color(204,0,0),sf::Color(204,102,0),sf::Color(204,204,0),sf::Color(102,204,0),sf::Color(0,204,102),sf::Color(0,204,204),sf::Color(0,102,204),sf::Color(102,0,204),sf::Color(204,0,204),sf::Color(204,0,102),sf::Color(96,96,96),sf::Color(255,0,0),sf::Color(255,128,0),sf::Color(255,255,0),sf::Color(128,255,0),sf::Color(0,255,128),sf::Color(0,255,255),sf::Color(0,128,255),sf::Color(127,0,255),sf::Color(255,0,255),sf::Color(255,0,127),sf::Color(128,128,128),sf::Color(255,153,51),sf::Color(51,51,255),sf::Color(153,51,255),sf::Color(255,51,153),sf::Color(160,160,160),sf::Color(255,102,102),sf::Color(255,178,102),sf::Color(178,255,102),sf::Color(102,102,255),sf::Color(178,102,255),sf::Color(255,102,255),sf::Color(255,102,178),sf::Color(192,192,192)};
    
    if(radius < pixelHorizontal){
        
        float angleIncrement = (10/static_cast<float>(radius));
        
        if(radius == 0){
            angleIncrement = 10;
        }
        
        for(int i=0; i < BVec.size();i++){
            int xPosNew,yPosNew;
            
            if(firstTimeVonori){
                
                vonoriPosX.push_back(BVec.at(i).returnPosBoid().returnX());
                vonoriPosY.push_back(BVec.at(i).returnPosBoid().returnY());
                
                BVec.at(i).setVonoriCellCollorOfBoid(static_cast<int>(colorVec[i].r), static_cast<int>(colorVec[i].g), static_cast<int>(colorVec[i].b));
                
            }
                
            
            
            if(BVec.size() < colorVec.size()){
                // Es sind nur 56 Boids zulässig
            }
                
            
            //for(float winkel=0;winkel <=360;winkel=winkel+0.01){
            for(float winkel=0;winkel <= 360;winkel=winkel+angleIncrement){
                float rad = M_PI/180*winkel;
                
                xPosNew=vonoriPosX[i]+(cos(rad)*radius-sin(rad)*0);
                yPosNew=vonoriPosY[i]+(sin(rad)*radius+cos(rad)*0);
                
                
                if((xPosNew >= 0 && xPosNew <= pixelHorizontal) && (yPosNew >= 0 && yPosNew <= pixelVertical)){
                
                   
                    if(xPosNew+yPosNew*pixelHorizontal > 0 && xPosNew+yPosNew*pixelHorizontal < (pixelHorizontal)*(pixelVertical)){
                        
                        if(noOtherColor((xPosNew+yPosNew*pixelHorizontal), BVec.size(), i)){
                            
                            vonoriVertexArray[xPosNew+yPosNew*pixelHorizontal].position=sf::Vector2f(xPosNew,yPosNew);
                        vonoriVertexArray[xPosNew+yPosNew*pixelHorizontal].color=sf::Color(static_cast<int>(colorVec[i].r),static_cast<int>(colorVec[i].g),static_cast<int>(colorVec[i].b));
                            
                        }
                         
                    }
                }
            }
        }
        firstTimeVonori = false;
    }
}

void vonoriCellsFieldDist(std::vector<Boid>& BVec){
    
    std::vector<sf::Color> colorVec{sf::Color(102,0,0),sf::Color(102,51,0),sf::Color(102,102,0),sf::Color(51,102,0),sf::Color(0,102,102),sf::Color(0,51,102),sf::Color(51,0,102),sf::Color(102,0,102),sf::Color(102,0,51),sf::Color(32,32,32),sf::Color(153,0,0),sf::Color(153,76,0),sf::Color(153,153,0),sf::Color(76,153,0),sf::Color(0,153,76),sf::Color(0,153,153),sf::Color(0,76,153),sf::Color(76,0,153),sf::Color(153,0,153),sf::Color(153,0,76),sf::Color(64,64,64),sf::Color(204,0,0),sf::Color(204,102,0),sf::Color(204,204,0),sf::Color(102,204,0),sf::Color(0,204,102),sf::Color(0,204,204),sf::Color(0,102,204),sf::Color(102,0,204),sf::Color(204,0,204),sf::Color(204,0,102),sf::Color(96,96,96),sf::Color(255,0,0),sf::Color(255,128,0),sf::Color(255,255,0),sf::Color(128,255,0),sf::Color(0,255,128),sf::Color(0,255,255),sf::Color(0,128,255),sf::Color(127,0,255),sf::Color(255,0,255),sf::Color(255,0,127),sf::Color(128,128,128),sf::Color(255,153,51),sf::Color(51,51,255),sf::Color(153,51,255),sf::Color(255,51,153),sf::Color(160,160,160),sf::Color(255,102,102),sf::Color(255,178,102),sf::Color(178,255,102),sf::Color(102,102,255),sf::Color(178,102,255),sf::Color(255,102,255),sf::Color(255,102,178),sf::Color(192,192,192)};
    
    for(int i=0; i < BVec.size();i++){
        
        std::map<int,int> distMap;
        int xPosNew,yPosNew;
        
        if(firstTimeVonori){
            
            vonoriPosX.push_back(BVec.at(i).returnPosBoid().returnX());
            vonoriPosY.push_back(BVec.at(i).returnPosBoid().returnY());
            
            BVec.at(i).setVonoriCellCollorOfBoid(static_cast<int>(colorVec[i].r), static_cast<int>(colorVec[i].g), static_cast<int>(colorVec[i].b));
            
        }
        
        for(int j=0;j<pixelHorizontal;j++){
            for(int k=0;k<pixelVertical;k++){
                float dist=sqrt(pow(j-vonoriPosX[i],2)+pow(k-vonoriPosY[i],2));
                distMap[j+k*pixelHorizontal]=dist;
            }
        }
        boidMapVector.push_back(distMap);
        
    }
    firstTimeVonori=false;
    
}

void vonoriCellsFieldDraw(std::vector<Boid>& BVec,int distance){
    
    std::vector<sf::Color> colorVec{sf::Color(102,0,0),sf::Color(102,51,0),sf::Color(102,102,0),sf::Color(51,102,0),sf::Color(0,102,102),sf::Color(0,51,102),sf::Color(51,0,102),sf::Color(102,0,102),sf::Color(102,0,51),sf::Color(32,32,32),sf::Color(153,0,0),sf::Color(153,76,0),sf::Color(153,153,0),sf::Color(76,153,0),sf::Color(0,153,76),sf::Color(0,153,153),sf::Color(0,76,153),sf::Color(76,0,153),sf::Color(153,0,153),sf::Color(153,0,76),sf::Color(64,64,64),sf::Color(204,0,0),sf::Color(204,102,0),sf::Color(204,204,0),sf::Color(102,204,0),sf::Color(0,204,102),sf::Color(0,204,204),sf::Color(0,102,204),sf::Color(102,0,204),sf::Color(204,0,204),sf::Color(204,0,102),sf::Color(96,96,96),sf::Color(255,0,0),sf::Color(255,128,0),sf::Color(255,255,0),sf::Color(128,255,0),sf::Color(0,255,128),sf::Color(0,255,255),sf::Color(0,128,255),sf::Color(127,0,255),sf::Color(255,0,255),sf::Color(255,0,127),sf::Color(128,128,128),sf::Color(255,153,51),sf::Color(51,51,255),sf::Color(153,51,255),sf::Color(255,51,153),sf::Color(160,160,160),sf::Color(255,102,102),sf::Color(255,178,102),sf::Color(178,255,102),sf::Color(102,102,255),sf::Color(178,102,255),sf::Color(255,102,255),sf::Color(255,102,178),sf::Color(192,192,192)};
    
    for(int i=0; i < BVec.size();i++){
        for(int j=0;j<pixelHorizontal;j++){
            for(int k=0; k < pixelVertical;k++){
                if(boidMapVector[i][j+k*pixelHorizontal] < distance){
                    if(noOtherColor(j+k*pixelHorizontal, BVec.size(), i)){
                        
                        vonoriVertexArray[j+k*pixelHorizontal].position=sf::Vector2f(j,k);
                    vonoriVertexArray[j+k*pixelHorizontal].color=sf::Color(static_cast<int>(colorVec[i].r),static_cast<int>(colorVec[i].g),static_cast<int>(colorVec[i].b));
                    }
                }
            }
        }
    }
}

float vonoriProgress(){
    float blackPixel = 0;
    float totalPixel = pixelHorizontal*pixelVertical;
    for(int i=0;i<totalPixel;i++){
        if(vonoriVertexArray[i].color == sf::Color::Black){
            blackPixel++;
        }
    }
    float result = (1 - (blackPixel/totalPixel))*100;
    
    return result;
}

void initVonoriVertexArray(){
    
    for(int i=0;i<((pixelVertical*pixelHorizontal)-1);i++){
        vonoriVertexArray[i].color = sf::Color::Black;
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                        //SIMULATION MAIN
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////


int main(){
    
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                        //INIT
    
    initPheromonVertexArray();
    initPheromon();
    initVonoriVertexArray();
    
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                        //GUI
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    sf::RectangleShape Limit(sf::Vector2f(300.f,pixelVertical));
    Limit.setFillColor(sf::Color(68,69,70));
    Limit.setPosition(pixelHorizontal, 0.f);
    
    sf::RectangleShape simRectBox(sf::Vector2f(200.f,50.f));
    simRectBox.setFillColor(sf::Color::Green);
    simRectBox.setPosition(pixelHorizontal+50, 15.f);
    
    sf::RectangleShape visualizationRectBox(sf::Vector2f(200.f,50.f));
    visualizationRectBox.setFillColor(sf::Color::Red);
    visualizationRectBox.setPosition(pixelHorizontal+50, 80.f);
    
    sf::RectangleShape pheromonRectBox(sf::Vector2f(200.f,50.f));
    pheromonRectBox.setFillColor(sf::Color::Red);
    pheromonRectBox.setPosition(pixelHorizontal+50, 145.f);
    
    sf::RectangleShape boundariesRectBox(sf::Vector2f(200.f,50.f));
    boundariesRectBox.setFillColor(sf::Color::Red);
    boundariesRectBox.setPosition(pixelHorizontal+50, 210.f);
    
    sf::RectangleShape targetRectBox(sf::Vector2f(200.f,50.f));
    targetRectBox.setFillColor(sf::Color::Red);
    targetRectBox.setPosition(pixelHorizontal+50, 275.f);
    
    sf::RectangleShape attackRectBox(sf::Vector2f(200.f,50.f));
    attackRectBox.setFillColor(sf::Color::Red);
    attackRectBox.setPosition(pixelHorizontal+50, 340.f);
    
    sf::RectangleShape vonoriRectBox(sf::Vector2f(200.f,50.f));
    vonoriRectBox.setFillColor(sf::Color::Red);
    vonoriRectBox.setPosition(pixelHorizontal+50, 405.f);
    
    sf::RectangleShape seperationRectBox(sf::Vector2f(200.f,50.f));
    seperationRectBox.setFillColor(sf::Color::Red);
    seperationRectBox.setPosition(pixelHorizontal+50, 470.f);
    
    sf::RectangleShape cozenageRectBox(sf::Vector2f(200.f,50.f));
    cozenageRectBox.setFillColor(sf::Color::Red);
    cozenageRectBox.setPosition(pixelHorizontal+50, 535.f);
    
    sf::RectangleShape protectRectBox(sf::Vector2f(200.f,50.f));
    protectRectBox.setFillColor(sf::Color::Red);
    protectRectBox.setPosition(pixelHorizontal+50, 600.f);
    
    sf::RectangleShape limitPerception(sf::Vector2f(275.f,50.f));
    limitPerception.setFillColor(sf::Color(68,69,70));
    limitPerception.setPosition(pixelHorizontal+15, 760.f);
    limitPerception.setOutlineThickness(2.f);
    limitPerception.setOutlineColor(sf::Color::White);
    
    sf::RectangleShape limitPheromonStrength(sf::Vector2f(275.f,50.f));
    limitPheromonStrength.setFillColor(sf::Color(68,69,70));
    limitPheromonStrength.setPosition(pixelHorizontal+15, 830.f);
    limitPheromonStrength.setOutlineThickness(2.f);
    limitPheromonStrength.setOutlineColor(sf::Color::White);
    
    sf::RectangleShape limitDecay(sf::Vector2f(275.f,50.f));
    limitDecay.setFillColor(sf::Color(68,69,70));
    limitDecay.setPosition(pixelHorizontal+15, 900.f);
    limitDecay.setOutlineThickness(2.f);
    limitDecay.setOutlineColor(sf::Color::White);
    
    sf::RectangleShape limitMaxGForce(sf::Vector2f(275.f,50.f));
    limitMaxGForce.setFillColor(sf::Color(68,69,70));
    limitMaxGForce.setPosition(pixelHorizontal+15, 970.f);
    limitMaxGForce.setOutlineThickness(2.f);
    limitMaxGForce.setOutlineColor(sf::Color::White);
    
    sf::RectangleShape limitMaxSpeed(sf::Vector2f(275.f,50.f));
    limitMaxSpeed.setFillColor(sf::Color(68,69,70));
    limitMaxSpeed.setPosition(pixelHorizontal+15, 1040.f);
    limitMaxSpeed.setOutlineThickness(2.f);
    limitMaxSpeed.setOutlineColor(sf::Color::White);
    
    sf::RectangleShape limitPheromonPerception(sf::Vector2f(275.f,50.f));
    limitPheromonPerception.setFillColor(sf::Color(68,69,70));
    limitPheromonPerception.setPosition(pixelHorizontal+15, 1110.f);
    limitPheromonPerception.setOutlineThickness(2.f);
    limitPheromonPerception.setOutlineColor(sf::Color::White);
    
    sf::RectangleShape limitTargetAcquiredTime(sf::Vector2f(275.f,50.f));
    limitTargetAcquiredTime.setFillColor(sf::Color(68,69,70));
    limitTargetAcquiredTime.setPosition(pixelHorizontal+15, 1180.f);
    limitTargetAcquiredTime.setOutlineThickness(2.f);
    limitTargetAcquiredTime.setOutlineColor(sf::Color::White);
    
    sf::RectangleShape limitProgressPheromon(sf::Vector2f(275.f,50.f));
    limitProgressPheromon.setFillColor(sf::Color(68,69,70));
    limitProgressPheromon.setPosition(pixelHorizontal+15, 1250);
    limitProgressPheromon.setOutlineThickness(2.f);
    limitProgressPheromon.setOutlineColor(sf::Color::White);
    
    sf::RectangleShape limitRadiusVonori(sf::Vector2f(275.f,50.f));
    limitRadiusVonori.setFillColor(sf::Color(68,69,70));
    limitRadiusVonori.setPosition(pixelHorizontal+15, 1320);
    limitRadiusVonori.setOutlineThickness(2.f);
    limitRadiusVonori.setOutlineColor(sf::Color::White);
    

    
    
    if(!font.loadFromFile("/Library/Fonts/Arial Unicode.ttf")){
        std::cout << "Fonts konnten nicht im angegebenen Verzeichnis gefunden werden" << std::endl;
    }
    
    sf::Text textActive;
    setStringCharakteristics(textActive,pixelHorizontal+80,20.f,"Simulation");
    
    sf::Text textVisualization;
    setStringCharakteristics(textVisualization, pixelHorizontal+65, 90, "Visualization");
    
    sf::Text textPheromon;
    setStringCharakteristics(textPheromon, pixelHorizontal+85, 155, "Pheromon");
    
    sf::Text textBoundaries;
    setStringCharakteristics(textBoundaries, pixelHorizontal+75, 220, "Boundaries");
    
    sf::Text textTarget;
    setStringCharakteristics(textTarget, pixelHorizontal+115, 285, "SEAD");
    
    sf::Text textAttack;
    setStringCharakteristics(textAttack, pixelHorizontal+110, 350, "Attack");
    
    sf::Text textVonori;
    setStringCharakteristics(textVonori, pixelHorizontal+105, 415, "Voronoi");
    
    sf::Text textSeperation;
    setStringCharakteristics(textSeperation, pixelHorizontal+75, 480, "Seperation");
    
    sf::Text textCozenage;
    setStringCharakteristics(textCozenage, pixelHorizontal+75, 545, "Cozennage");
    
    sf::Text textProtect;
    setStringCharakteristics(textProtect, pixelHorizontal+105, 610, "Protect");
    
    
    
    
    
    sf::Text textPerception;
    setStringCharakteristics(textPerception, pixelHorizontal+25, 767, "Perception");
    sf::Text perceptionFloat;
    int a = static_cast<int>(perception);
    std::string p = std::to_string(a);
    setStringCharakteristics(perceptionFloat, pixelHorizontal+200, 767, p);
    
    sf::Text textPheromonStrength;
    setStringCharakteristics(textPheromonStrength, pixelHorizontal+25, 837, "P Strength:");
    sf::Text pheromonStrengthFloat;
    int pStrength = static_cast<int>(pheromonStrength);
    std::string strengthStr = std::to_string(pStrength);
    setStringCharakteristics(pheromonStrengthFloat, pixelHorizontal+200, 837, strengthStr);
    
    sf::Text textDecay;
    setStringCharakteristics(textDecay, pixelHorizontal+20, 910, "decayRate");
    sf::Text decayFloat;
    setStringCharakteristics(decayFloat, pixelHorizontal+200, 910, limitFloat2String(decayRate, 2));
    
    sf::Text textMaxGForce;
    setStringCharakteristics(textMaxGForce, pixelHorizontal+25, 977, "Max G Force:");
    sf::Text maxGFloat;
    setStringCharakteristics(maxGFloat, pixelHorizontal+220, 977, limitFloat2String(maxGForce, 2));
    
    sf::Text textMaxSpeed;
    setStringCharakteristics(textMaxSpeed, pixelHorizontal+25, 1047, "Max Speed:");
    sf::Text maxSpeedFloat;
    int maxSp = static_cast<int>(maxSpeed);
    std::string SpeedStr = std::to_string(maxSp);
    setStringCharakteristics(maxSpeedFloat, pixelHorizontal+200, 1047, SpeedStr);
    
    sf::Text textPheromonPerception;
    setStringCharakteristics(textPheromonPerception, pixelHorizontal+25, 1117, "PheroPrcpt.:");
    sf::Text pheromonPerceptionFloat;
    int pheroPer = static_cast<int>(pheromonPerception);
    std::string pheroPerStr = std::to_string(pheroPer);
    setStringCharakteristics(pheromonPerceptionFloat, pixelHorizontal+210, 1117, pheroPerStr);
    
    sf::Text textTargetAcquiredTime;
    setStringCharakteristics(textTargetAcquiredTime, pixelHorizontal+25, 1187, "TangoAcq. in:");
    sf::Text TargetAcquiredTimeFloat;
    int AcquiredTarget = static_cast<int>(time2TargetAcquired);
    std::string AcquiredTargetStr = std::to_string(AcquiredTarget);
    setStringCharakteristics(TargetAcquiredTimeFloat, pixelHorizontal+230, 1187, AcquiredTargetStr);
    
    sf::Text textProgressPheromon;
    setStringCharakteristics(textProgressPheromon, pixelHorizontal+25, 1257, "PheroProgrs:");
    sf::Text progressPheromonFloat;
    int pheroProgress = static_cast<int>(progressPheromonFloatVariable);
    std::string pheroProgressStr = std::to_string(pheroProgress);
    setStringCharakteristics(progressPheromonFloat, pixelHorizontal+210, 1257, pheroProgressStr);
    sf::Text textProgressPheromonProzent;
    setStringCharakteristics(textProgressPheromonProzent, pixelHorizontal+260, 1257, "%");
    
    sf::Text textVonoriRadius;
    setStringCharakteristics(textVonoriRadius, pixelHorizontal+25, 1327, "Radius: ");
    sf::Text textVonoriRadiusFloat;
    int radiusFloat = static_cast<int>(radiusVonori);
    std::string radiusFloatStr = std::to_string(radiusFloat);
    setStringCharakteristics(textVonoriRadiusFloat, pixelHorizontal+140, 1327, radiusFloatStr);
    
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    sf::RenderWindow window;
    std::vector<Boid> ActiveBoids;
    std::vector<Obstacle> ActiveObstacles;
    std::vector<Target> ActiveTargets;
    std::vector<AircraftFriend> ActiveAircraftsFriend;
    std::vector<AircraftFoe> ActiveAircraftsFoe;
    std::vector<std::vector<PVector>> BoidsAssignedToTargetVector2D;
    sf::Clock clock;
    sf::Clock TargetClock;
    
    sf::FloatRect boundingBoxP = limitPerception.getGlobalBounds();
    sf::FloatRect boundingBoxStrength = limitPheromonStrength.getGlobalBounds();
    sf::FloatRect boundingBoxDecay = limitDecay.getGlobalBounds();
    sf::FloatRect boundingBoxG = limitMaxGForce.getGlobalBounds();
    sf::FloatRect boundingBoxSpeed = limitMaxSpeed.getGlobalBounds();
    sf::FloatRect boundingBoxPheroPercept = limitPheromonPerception.getGlobalBounds();
    
    sf::FloatRect boundingBoxSimulation = simRectBox.getGlobalBounds();
    sf::FloatRect boundingBoxVisualization = visualizationRectBox.getGlobalBounds();
    sf::FloatRect boundingBoxPheromon = pheromonRectBox.getGlobalBounds();
    sf::FloatRect boundingBoxBoundary = boundariesRectBox.getGlobalBounds();
    sf::FloatRect boundingBoxTarget = targetRectBox.getGlobalBounds();
    sf::FloatRect boundingBoxAttack = attackRectBox.getGlobalBounds();
    sf::FloatRect boundingBoxVonori = vonoriRectBox.getGlobalBounds();
    sf::FloatRect boundingBoxSeperation = seperationRectBox.getGlobalBounds();
    sf::FloatRect boundingBoxCozenage = cozenageRectBox.getGlobalBounds();
    sf::FloatRect boundingBoxProtect = protectRectBox.getGlobalBounds();
    
    window.create(sf::VideoMode(pixelHorizontal+300,pixelVertical), "Swarm Visualization Window",sf::Style::Default);
    window.setPosition(sf::Vector2i(300,200));
    
    //////////////// Setze Hindernisee ///////////////
    /*for(int i=0; i<70; i++){
        Obstacle Obstacle(200,20*i);
        ActiveObstacles.push_back(Obstacle);
    }
    for(int i=0; i<70; i++){
        Obstacle Obstacle(1800,20*i);
        ActiveObstacles.push_back(Obstacle);
    }
    for(int i=10; i < 90;i++){
        Obstacle Obstacle(20*i,200);
        ActiveObstacles.push_back(Obstacle);
    }
    for(int i=10; i < 90;i++){
        Obstacle Obstacle(20*i,1200);
        ActiveObstacles.push_back(Obstacle);
    }*/
    /////////////////////////////////////////////////////
    
    /////////////// Setze Boids /////////////////////////
    /*for(int i=950;i<1050;i++){
        Boid Boid(i,700);
        ActiveBoids.push_back(Boid);
    }*/

    
    
    while(window.isOpen()){
        
        sf::Event event;
        sf::Vector2i MousePos = sf::Mouse::getPosition(window);
        
        
        
 ////////////////////////////////////////////////////////////////////////////////////////////
                            //SCANNING FOR INPUT
        sf::Time elapsed = clock.getElapsedTime();
        
        
        if(elapsed.asSeconds() > 0.0333){
        
            while(window.pollEvent(event)){
            
                if(event.type == sf::Event::Closed){
                    window.close();
                }
                if (event.type == sf::Event::Resized)
                {
                    /*sf::FloatRect visibleArea(0, 0, event.size.width, event.size.height);
                    window.setView(sf::View(visibleArea));*/
                }
                
                // TASTATUREINGABE
                if(event.type == sf::Event::KeyPressed){
                
                    if(event.key.code == sf::Keyboard::Escape){
                        window.close();
                    }
                    if(event.key.code == sf::Keyboard::T && (MousePos.x < pixelHorizontal && MousePos.y < pixelVertical) ){
                        Target Target(MousePos.x, MousePos.y);
                        ActiveTargets.push_back(Target);
                    }
                    if(event.key.code == sf::Keyboard::A && (MousePos.x < pixelHorizontal && MousePos.y < pixelVertical)){
                        AircraftFriend Aircraft(MousePos.x,MousePos.y);
                        ActiveAircraftsFriend.push_back(Aircraft);
                    }
                    if(event.key.code == sf::Keyboard::E && (MousePos.x < pixelHorizontal && MousePos.y < pixelVertical)){
                        AircraftFoe Aircraft(MousePos.x,MousePos.y);
                        ActiveAircraftsFoe.push_back(Aircraft);
                    }
                    if(event.key.code == sf::Keyboard::Space && simulationActive == true){
                        simulationActive = false;
                        simRectBox.setFillColor(sf::Color::Red);
                    }else if(event.key.code == sf::Keyboard::Space && simulationActive == false){
                        simulationActive = true;
                        simRectBox.setFillColor(sf::Color::Green);
                    }
                    
                    if(event.key.code == sf::Keyboard::V && visualizationActive == true){
                        visualizationActive = false;
                        visualizationRectBox.setFillColor(sf::Color::Red);
                        for(int i=0;i<pixelHorizontal;i++){
                            pheromonArray.at(i).fill({});
                        }
                    }else if(event.key.code == sf::Keyboard::V && visualizationActive == false){
                        visualizationActive = true;
                        visualizationRectBox.setFillColor(sf::Color::Green);
                    }
                    
                    if(event.key.code == sf::Keyboard::P && pheromonActive == true){
                        pheromonActive = false;
                        pheromonRectBox.setFillColor(sf::Color::Red);
                    }else if(event.key.code == sf::Keyboard::P && pheromonActive == false){
                        pheromonActive = true;
                        pheromonRectBox.setFillColor(sf::Color::Green);
                    }
                    
                    if(event.key.code == sf::Keyboard::B && boundariesActive == true){
                        boundariesActive = false;
                        boundariesRectBox.setFillColor(sf::Color::Red);
                    }else if(event.key.code == sf::Keyboard::B && boundariesActive == false){
                        boundariesActive = true;
                        boundariesRectBox.setFillColor(sf::Color::Green);
                    }
                    
                    if(event.key.code == sf::Keyboard::J && jamSamSitesActive == true){
                        jamSamSitesActive=false;
                        targetRectBox.setFillColor(sf::Color::Red);
                    }else if(event.key.code == sf::Keyboard::J && jamSamSitesActive == false){
                        jamSamSitesActive=true;
                        targetRectBox.setFillColor(sf::Color::Green);
                    }
                    
                    if(event.key.code == sf::Keyboard::S && AttackActive == true){
                        AttackActive=false;
                        attackRectBox.setFillColor(sf::Color::Red);
                    }else if(event.key.code == sf::Keyboard::S && AttackActive == false){
                        AttackActive=true;
                        attackRectBox.setFillColor(sf::Color::Green);
                    }
                }
                
                // MOUSEEINGABE
                if(event.type == sf::Event::MouseButtonPressed){
                    if(event.mouseButton.button == sf::Mouse::Right && MousePos.x < pixelHorizontal && MousePos.y < pixelVertical){
                        Obstacle Obstacle(MousePos.x,MousePos.y);
                        ActiveObstacles.push_back(Obstacle);
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && MousePos.x < pixelHorizontal && MousePos.y < pixelVertical){
                        Boid Boid(MousePos.x,MousePos.y);
                        ActiveBoids.push_back(Boid);
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && boundingBoxP.contains(MousePos.x, MousePos.y)){
                        perception++;
                        int a = static_cast<int>(perception);
                        std::string p = std::to_string(a);
                        perceptionFloat.setString(p);
                    }
                    if(event.mouseButton.button == sf::Mouse::Right && boundingBoxP.contains(MousePos.x, MousePos.y)){
                        perception--;
                        int a = static_cast<int>(perception);
                        std::string p = std::to_string(a);
                        perceptionFloat.setString(p);
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && boundingBoxStrength.contains(MousePos.x, MousePos.y)){
                        pheromonStrength++;
                        int pStrength = static_cast<int>(pheromonStrength);
                        std::string strengthStr = std::to_string(pStrength);
                        pheromonStrengthFloat.setString(strengthStr);
                    }
                    if(event.mouseButton.button == sf::Mouse::Right && boundingBoxStrength.contains(MousePos.x, MousePos.y)){
                        pheromonStrength--;
                        int pStrength = static_cast<int>(pheromonStrength);
                        std::string strengthStr = std::to_string(pStrength);
                        pheromonStrengthFloat.setString(strengthStr);
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && boundingBoxSpeed.contains(MousePos.x, MousePos.y)){
                        maxSpeed++;
                        int maxSp = static_cast<int>(maxSpeed);
                        std::string SpeedStr = std::to_string(maxSp);
                        maxSpeedFloat.setString(SpeedStr);
                    }
                    if(event.mouseButton.button == sf::Mouse::Right && boundingBoxSpeed.contains(MousePos.x, MousePos.y)){
                        maxSpeed--;
                        int maxSp = static_cast<int>(maxSpeed);
                        std::string SpeedStr = std::to_string(maxSp);
                        maxSpeedFloat.setString(SpeedStr);
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && boundingBoxG.contains(MousePos.x, MousePos.y)){
                        maxGForce = maxGForce+0.1;
                        maxGFloat.setString(limitFloat2String(maxGForce, 2));
                    }
                    if(event.mouseButton.button == sf::Mouse::Right && boundingBoxG.contains(MousePos.x, MousePos.y)){
                        maxGForce = maxGForce-0.1;
                        maxGFloat.setString(limitFloat2String(maxGForce, 2));
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && boundingBoxDecay.contains(MousePos.x, MousePos.y)){
                        decayRate = decayRate+0.1;
                        decayFloat.setString(limitFloat2String(decayRate, 2));
                    }
                    if(event.mouseButton.button == sf::Mouse::Right && boundingBoxDecay.contains(MousePos.x, MousePos.y)){
                        decayRate = decayRate-0.1;
                        decayFloat.setString(limitFloat2String(decayRate, 2));
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && boundingBoxPheroPercept.contains(MousePos.x,MousePos.y)){
                        pheromonPerception = pheromonPerception+1;
                        int pheroPer = static_cast<int>(pheromonPerception);
                        std::string pheroPerStr = std::to_string(pheroPer);
                        pheromonPerceptionFloat.setString(pheroPerStr);
                    }
                    if(event.mouseButton.button == sf::Mouse::Right && boundingBoxPheroPercept.contains(MousePos.x, MousePos.y)){
                        pheromonPerception = pheromonPerception-1;
                        int pheroPer = static_cast<int>(pheromonPerception);
                        std::string pheroPerStr = std::to_string(pheroPer);
                        pheromonPerceptionFloat.setString(pheroPerStr);
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && boundingBoxSimulation.contains(MousePos.x, MousePos.y)){
                        simulationActive = true;
                        simRectBox.setFillColor(sf::Color::Green);
                    }
                    if(event.mouseButton.button == sf::Mouse::Right && boundingBoxSimulation.contains(MousePos.x, MousePos.y)){
                        simulationActive = false;
                        simRectBox.setFillColor(sf::Color::Red);
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && boundingBoxVisualization.contains(MousePos.x,MousePos.y)){
                        visualizationActive = true;
                        visualizationRectBox.setFillColor(sf::Color::Green);
                    }
                    if(event.mouseButton.button == sf::Mouse::Right && boundingBoxVisualization.contains(MousePos.x,MousePos.y)){
                        visualizationActive = false;
                        visualizationRectBox.setFillColor(sf::Color::Red);
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && boundingBoxPheromon.contains(MousePos.x, MousePos.y)){
                        pheromonActive = true;
                        pheromonRectBox.setFillColor(sf::Color::Green);
                    }
                    if(event.mouseButton.button == sf::Mouse::Right && boundingBoxPheromon.contains(MousePos.x, MousePos.y)){
                        pheromonActive = false;
                        pheromonRectBox.setFillColor(sf::Color::Red);
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && boundingBoxBoundary.contains(MousePos.x, MousePos.y)){
                        boundariesActive = true;
                        boundariesRectBox.setFillColor(sf::Color::Green);
                    }
                    if(event.mouseButton.button == sf::Mouse::Right && boundingBoxBoundary.contains(MousePos.x, MousePos.y)){
                        boundariesActive = false;
                        boundariesRectBox.setFillColor(sf::Color::Red);
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && boundingBoxTarget.contains(MousePos.x, MousePos.y)){
                        jamSamSitesActive = true;
                        targetRectBox.setFillColor(sf::Color::Green);
                    }
                    if(event.mouseButton.button == sf::Mouse::Right && boundingBoxTarget.contains(MousePos.x, MousePos.y)){
                        jamSamSitesActive = false;
                        targetRectBox.setFillColor(sf::Color::Red);
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && boundingBoxAttack.contains(MousePos.x, MousePos.y)){
                        AttackActive = true;
                        attackRectBox.setFillColor(sf::Color::Green);
                    }
                    if(event.mouseButton.button == sf::Mouse::Right && boundingBoxAttack.contains(MousePos.x, MousePos.y)){
                        AttackActive = false;
                        attackRectBox.setFillColor(sf::Color::Red);
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && boundingBoxVonori.contains(MousePos.x, MousePos.y)){
                        vonoriActive = true;
                        vonoriRectBox.setFillColor(sf::Color::Green);
                    }
                    if(event.mouseButton.button == sf::Mouse::Right && boundingBoxVonori.contains(MousePos.x, MousePos.y)){
                        vonoriActive = false;
                        vonoriRectBox.setFillColor(sf::Color::Red);
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && boundingBoxSeperation.contains(MousePos.x,MousePos.y)){
                        seperationActive = true;
                        seperationRectBox.setFillColor(sf::Color::Green);
                    }
                    if(event.mouseButton.button == sf::Mouse::Right && boundingBoxSeperation.contains(MousePos.x,MousePos.y)){
                        seperationActive = false;
                        seperationRectBox.setFillColor(sf::Color::Red);
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && boundingBoxCozenage.contains(MousePos.x,MousePos.y)){
                        cozenageRectBox.setFillColor(sf::Color::Green);
                    }
                    if(event.mouseButton.button == sf::Mouse::Right && boundingBoxCozenage.contains(MousePos.x,MousePos.y)){
                        cozenageRectBox.setFillColor(sf::Color::Red);
                    }
                    if(event.mouseButton.button == sf::Mouse::Left && boundingBoxProtect.contains(MousePos.x, MousePos.y)){
                        protectRectBox.setFillColor(sf::Color::Green);
                        protectAircraftActive = true;
                    }
                    if(event.mouseButton.button == sf::Mouse::Right && boundingBoxProtect.contains(MousePos.x, MousePos.y)){
                        protectRectBox.setFillColor(sf::Color::Red);
                        protectAircraftActive = false;
                    }
                }
            }
///////////////////////////////////////////////////////////////////////////////////////////////////
                                    //SIMULATION
        
            
            window.clear(sf::Color::Black);
            
            
            if(vonoriActive){
                
                // VONORI CELSS VIA RADIUS
                
                float progress=vonoriProgress();
                
                if(progress < 99.999){
                    simulationActive = false;
                    simRectBox.setFillColor(sf::Color::Red);
                }
                vonoriCells(ActiveBoids,radiusVonori);
                
                if(progress < 99.999){
                    radiusVonori++;
                }
                if(progress > 99.999f && !restartSim){
                    restartSim=true;
                    simulationActive = true;
                    simRectBox.setFillColor(sf::Color::Green);
                }
                
                
                /*
                if(radiusVonori < 501){
                    simulationActive = false;
                    simRectBox.setFillColor(sf::Color::Red);
                 }
                vonoriCells(ActiveBoids,radiusVonori);
                 
                if(radiusVonori < 501){
                    radiusVonori++;
                }
                if(radiusVonori > 500 && ! restartSim){
                    restartSim=true;
                    simulationActive = true;
                    simRectBox.setFillColor(sf::Color::Green);
                }
                */
                
                
                int radiusFloat = static_cast<int>(radiusVonori);
                std::string radiusFloatStr = std::to_string(radiusFloat);
                textVonoriRadiusFloat.setString(radiusFloatStr);
                
                
                // VONORI CELLS VIA DISTANCE
                /*
                float progress=vonoriProgress();
                
                if(firstT){
                    vonoriCellsFieldDist(ActiveBoids);
                    firstT=false;
                }
                
                if(progress < 99.999){
                    vonoriCellsFieldDraw(ActiveBoids, distVonori);
                    distVonori++;
                }else if(progress > 99.999){
                    simulationActive = true;
                    simRectBox.setFillColor(sf::Color::Green);
                }*/
                
                window.draw(vonoriVertexArray);
            }
            
            if(visualizationActive){
                for(int i=0;i<=pixelHorizontal;i++){
                    for(int j=0;j<=pixelVertical;j++){
                        if(simulationActive){
                            fadingPheromon(i,j);
                        }
                        /* //Es werden nicht alle Spalten gefüllt
                        if(pheromonArray[i][j] > 0.0001){
                            pheromonVertexArray[i+j*pixelHorizontal].position=sf::Vector2f(0.f+i,0.f+j);
                            colorStruct tempCol= setPheromonColour(i,j);
                            pheromonVertexArray[i+j*pixelHorizontal].color=sf::Color(tempCol.r,tempCol.g,tempCol.b);
                        }
                         */
                        if(pheromonArray[i][j] > 0.0001){
                            pheromonVertexArray[j+i*pixelVertical+i].position=sf::Vector2f(0.f+i,0.f+j);
                            colorStruct tempCol= setPheromonColour(i,j);
                            pheromonVertexArray[j+i*pixelVertical+i].color=sf::Color(tempCol.r,tempCol.g,tempCol.b);
                        }
                    }
                }
            
            window.draw(pheromonVertexArray);
            }
            
            
            for(auto ObstIt:ActiveObstacles){
                window.draw(ObstIt.returnObstacle());
            }
            
            for(auto TarIt:ActiveTargets){
                window.draw(TarIt.returnTarget());
            }
            
        
            if(simulationActive){
                for(int i=0; i < ActiveTargets.size(); i++){
                    BoidsAssignedToTargetVector2D.push_back(std::vector<PVector>(1,ActiveTargets.at(i).returnTargetPos()));
                }
                for(int i=0; i < ActiveBoids.size();i++){
                    ActiveBoids.at(i).flock(&ActiveBoids,&ActiveObstacles,&ActiveTargets,&ActiveAircraftsFriend,&ActiveAircraftsFoe,&BoidsAssignedToTargetVector2D);
                    if(visualizationActive){
                        updatePheromon(ActiveBoids.at(i).returnPosBoid(), ActiveBoids.at(i).returnPerception());
                    }
                    ActiveBoids.at(i).updateBoids();
                }
                BoidsAssignedToTargetVector2D.clear();
                for(int i=0; i < ActiveAircraftsFriend.size();i++){
                    ActiveAircraftsFriend.at(i).calculateNewPosition();
                    ActiveAircraftsFriend.at(i).updateAcfs();
                }
                for(int i=0; i < ActiveAircraftsFoe.size();i++){
                    ActiveAircraftsFoe.at(i).calculateNewPosition();
                    ActiveAircraftsFoe.at(i).updateAcfs();
                }
            }
            
            for(int i=0; i < ActiveBoids.size();i++){
                window.draw(ActiveBoids.at(i).returnBoid());
            }
            for(auto AcfItBlue:ActiveAircraftsFriend){
                window.draw(AcfItBlue.returnAcf());
            }
            for(auto AcfItRed:ActiveAircraftsFoe){
                window.draw(AcfItRed.returnAcf());
            }
            
            
            if(targetAcquired){
                targetAcquired = false;
                
                sf::Time targetTime = TargetClock.getElapsedTime();
                time2TargetAcquired = targetTime.asSeconds();
                int AcquiredTarget = static_cast<int>(time2TargetAcquired);
                std::string AcquiredTargetStr = std::to_string(AcquiredTarget);
                TargetAcquiredTimeFloat.setString(AcquiredTargetStr);
                std::cout << "Target Acquired in: " << AcquiredTarget << " Seconds" << std::endl;
            }
            
            /////////        GUI          /////////////
            
            
            if(pheromonActive || visualizationActive){
                progressPheromonFloatVariable = progressPheromon();
                int pheroProgress = static_cast<int>(progressPheromonFloatVariable);
                std::string pheroProgressStr = std::to_string(pheroProgress);
                progressPheromonFloat.setString(pheroProgressStr);
            }
            
            window.draw(Limit);
            window.draw(limitPerception);
            window.draw(simRectBox);
            window.draw(visualizationRectBox);
            window.draw(limitPheromonStrength);
            window.draw(limitDecay);
            window.draw(limitMaxGForce);
            window.draw(limitMaxSpeed);
            window.draw(limitPheromonPerception);
            window.draw(boundariesRectBox);
            window.draw(targetRectBox);
            window.draw(attackRectBox);
            window.draw(limitTargetAcquiredTime);
            window.draw(vonoriRectBox);
            window.draw(limitProgressPheromon);
            window.draw(limitRadiusVonori);
            window.draw(seperationRectBox);
            window.draw(cozenageRectBox);
            window.draw(protectRectBox);
            
            window.draw(textPerception);
            window.draw(perceptionFloat);
            window.draw(textActive);
            window.draw(textVisualization);
            window.draw(textDecay);
            window.draw(decayFloat);
            window.draw(textPheromonStrength);
            window.draw(pheromonStrengthFloat);
            window.draw(textMaxGForce);
            window.draw(maxGFloat);
            window.draw(textMaxSpeed);
            window.draw(maxSpeedFloat);
            window.draw(pheromonRectBox);
            window.draw(textPheromon);
            window.draw(textPheromonPerception);
            window.draw(pheromonPerceptionFloat);
            window.draw(textBoundaries);
            window.draw(textTarget);
            window.draw(textAttack);
            window.draw(textTargetAcquiredTime);
            window.draw(TargetAcquiredTimeFloat);
            window.draw(textVonori);
            window.draw(textProgressPheromon);
            window.draw(progressPheromonFloat);
            window.draw(textProgressPheromonProzent);
            window.draw(textVonoriRadius);
            window.draw(textVonoriRadiusFloat);
            window.draw(textSeperation);
            window.draw(textCozenage);
            window.draw(textProtect);
            
            
            //////////////////////
        
            window.display();
        
            clock.restart();
            }
       }
    
    return 0;
}
